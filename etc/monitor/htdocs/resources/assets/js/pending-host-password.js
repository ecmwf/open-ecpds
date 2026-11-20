(() => {
    'use strict';
    const modal = document.getElementById('pendingHostPasswordModal');
    if (!modal) return;

    const status = modal.querySelector('[data-rotation-status]');
    const input = modal.querySelector('[data-rotation-input]');
    const password = input.querySelector('input');
    const grace = modal.querySelector('[data-rotation-grace]');
    const graceRow = modal.querySelector('[data-rotation-grace-row]');
    const label = modal.querySelector('[data-rotation-label]');
    const buttons = [...modal.querySelectorAll('button[data-operation]')];
    const states = {
        STAGING: ['Staging is unconfirmed. Reconcile before continuing.', 'RECONCILE'],
        STAGED: ['Replacement staged. Test it before activation.', 'PROBE', 'DISCARD'],
        TESTED: ['Replacement passed a probe. Activation is not available here.', 'PROBE', 'DISCARD'],
        DISCARDING: ['Discard is unconfirmed. Reconcile before continuing.', 'RECONCILE'],
    };
    const probeResults = {
        PASSED: 'Latest test passed.',
        CONNECTION_OR_AUTH_FAILED: 'Latest test could not connect or authenticate.',
        WRITE_FAILED: 'Latest test could not write to the approved probe directory.',
        CLEANUP_FAILED: 'Latest test could not confirm probe cleanup and session closure. Contact an operator.',
    };
    let active;

    function render(message, actions = []) {
        status.textContent = message;
        const staging = actions.includes('STAGE');
        input.hidden = password.disabled = !(staging || actions.includes('ACTIVATE') && active?.requiresPassword);
        if (graceRow) graceRow.hidden = !staging;
        if (label) label.textContent = staging ? 'Replacement transfer credential' : 'Current Monitor password';
        const credentialHelp = input.querySelector('[data-rotation-credential-help]');
        if (credentialHelp) credentialHelp.hidden = !staging;
        for (const button of buttons) {
            button.hidden = !actions.includes(button.dataset.operation);
            button.disabled = button.hidden;
        }
    }

    async function request(context, operation) {
        if (context.busy) return;
        const requestedReference = context.reference;
        const options = { credentials: 'same-origin', cache: 'no-store', redirect: 'error' };
        if (operation !== 'REFRESH') {
            if (!context.csrf) return;
            if (operation === 'STAGE' && !password.value) {
                status.textContent = 'Enter a replacement credential to stage.';
                password.focus();
                return;
            }
            if (operation === 'DISCARD' && !window.confirm('Discard this pending replacement?')) return;
            if (operation === 'DRAIN' && !window.confirm(
                'Fence legacy connections for this Host across the fleet? This persists across restart and cannot be undone here.')) return;
            const hours = Number(grace?.value ?? 24);
            if (operation === 'STAGE' && (!Number.isInteger(hours) || hours < 1 || hours > 720)) {
                status.textContent = 'Choose a whole number of retirement hours from 1 to 720.';
                return;
            }
            if (operation === 'ACTIVATE' && context.requiresPassword && !password.value) {
                status.textContent = 'Enter your current Monitor password to confirm activation.';
                password.focus();
                return;
            }
            options.method = 'POST';
            options.headers = {
                'Content-Type': 'application/json',
                'X-ECPDS-Rotation-CSRF': context.csrf,
            };
            options.body = JSON.stringify(operation === 'STAGE'
                ? { operation, password: password.value, retirementGraceHours: hours }
                : operation === 'ACTIVATE'
                    ? { operation, reference: requestedReference, state: context.activationState,
                        ...(context.requiresPassword ? { password: password.value } : {}) }
                    : { operation, reference: requestedReference });
        }
        password.value = '';
        context.busy = true;
        context.reference = null;
        render('Waiting for the Master…');
        try {
            const response = await fetch(context.url, options);
            if (!response.ok) throw new Error();
            const result = await response.json();
            if (active !== context) return;
            if (operation === 'BEGIN_ACTIVATION') {
                if (typeof result.state !== 'string') throw new Error();
                context.reference = requestedReference;
                context.activationState = result.state;
                context.requiresPassword = result.localPasswordRequired === true;
                if (result.authorizationUri) {
                    const target = new URL(result.authorizationUri);
                    if (target.protocol !== 'https:' || target.username || target.password) throw new Error();
                    window.location.assign(target.href);
                } else {
                    render(context.requiresPassword ? 'Re-enter your Monitor password to activate.'
                        : 'Fresh MFA verified. Confirm activation.', ['ACTIVATE', 'REFRESH']);
                }
                return;
            }
            if (operation === 'ACTIVATE') {
                if (result.activated !== true) throw new Error();
                context.activationState = null;
                render('Credential activated. Refresh to confirm the active reference.', ['REFRESH']);
                return;
            }
            if (operation === 'REFRESH') {
                context.csrf = response.headers.get('X-ECPDS-Rotation-CSRF');
                if (!context.csrf) throw new Error();
            }
            if (result.state === null && result.reference === null) {
                render(result.activeReference ? 'A vault-backed credential is active. No pending replacement.'
                    : 'No pending replacement.', ['STAGE', 'REFRESH']);
            } else if (Object.hasOwn(states, result.state) && typeof result.reference === 'string') {
                context.reference = result.reference;
                let [message, ...actions] = states[result.state];
                const probe = Object.hasOwn(probeResults, result.probe) ? ' ' + probeResults[result.probe] : '';
                if (result.state === 'TESTED' && result.activationEnabled === true) {
                    message = 'Replacement passed a probe. Prepare cutover, then reauthenticate to activate.';
                    const drain = result.drain;
                    if (result.activeReference || drain && drain.routes > 0
                        && drain.routes === drain.freshReceipts && drain.sessions === 0 && drain.unresolvedAdmissions === 0) {
                        actions.push('BEGIN_ACTIVATION');
                    }
                    if (!result.activeReference) actions.push('DRAIN');
                }
                if (result.drain?.routes > 0) {
                    const drain = result.drain;
                    message += ` Drain: ${drain.freshReceipts}/${drain.routes} fresh receipts, `
                        + `${drain.sessions} sessions, ${drain.unresolvedAdmissions} unresolved admissions.`;
                }
                if (Number.isInteger(result.retirementGraceSeconds)) {
                    message += ` Retirement grace: ${result.retirementGraceSeconds / 3600} hours after confirmed delivery.`;
                }
                render(message + probe, [...actions, 'REFRESH']);
            } else {
                throw new Error();
            }
        } catch {
            if (active === context) {
                context.csrf = null;
                render(operation === 'REFRESH'
                    ? 'Pending credential status is unavailable. Check access or sign in again.'
                    : 'Operation could not be confirmed. Refresh before trying again.', ['REFRESH']);
            }
        } finally {
            context.busy = false;
        }
    }

    modal.addEventListener('show.bs.modal', event => {
        const host = event.relatedTarget?.dataset.rotationHost;
        if (!/^[1-9]\d*$/.test(host || '')) {
            event.preventDefault();
            return;
        }
        const destination = encodeURIComponent(modal.dataset.destination);
        active = { url: `/do/transfer/destination/passwordRotation/${destination}/${host}` };
        request(active, 'REFRESH');
    });
    modal.addEventListener('hide.bs.modal', () => {
        active = null;
        password.value = '';
        render('');
    });
    for (const button of buttons) {
        button.addEventListener('click', () => {
            if (active && !button.disabled) request(active, button.dataset.operation);
        });
    }
    const resume = /^#password-rotation-([1-9]\d*)$/.exec(window.location?.hash || '');
    if (resume) document.querySelector(`[data-rotation-host="${resume[1]}"]`)?.click();
})();
