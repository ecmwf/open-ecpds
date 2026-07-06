<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>
<c:set var="_guideId" value="${not empty param.guideId ? param.guideId : 'moduleGuideOffcanvas'}"/>

<%-- Amazon S3 Module Configuration Guide - offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="${_guideId}"
     aria-labelledby="moduleGuideLabel" style="width:720px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="moduleGuideLabel">
      <i class="bi bi-book me-2 text-info"></i>Amazon S3 Module &mdash; Configuration Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div><strong>AWS SDK v2</strong> &mdash; this module uses the AWS Java SDK v2
      (<code>software.amazon.awssdk</code>). Credentials are taken from the host
      <em>Login</em> (Access Key ID) and <em>Password</em> (Secret Access Key) fields.
      IAM role assumption is also supported.</div>
    </div>

    <ul class="nav nav-tabs nav-fill mb-3" id="s3GuideTab" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active small py-1" data-bs-toggle="tab"
                data-bs-target="#s3gt-connection" type="button" role="tab">
          <i class="bi bi-plug me-1"></i>Connection
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#s3gt-auth" type="button" role="tab">
          <i class="bi bi-shield-lock me-1"></i>Auth &amp; IAM
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#s3gt-bucket" type="button" role="tab">
          <i class="bi bi-bucket me-1"></i>Bucket
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link small py-1" data-bs-toggle="tab"
                data-bs-target="#s3gt-transfer" type="button" role="tab">
          <i class="bi bi-arrow-up-down me-1"></i>Transfer
        </button>
      </li>
    </ul>

    <div class="tab-content" id="s3GuideTabContent">

      <%-- ================================================================
           TAB 1: CONNECTION
           ================================================================ --%>
      <div class="tab-pane fade show active" id="s3gt-connection" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-cloud text-primary me-1"></i>AWS endpoint</p>
          <p class="small text-muted mb-1">The module connects to standard AWS endpoints.
          Use <code>s3.url</code> for S3-compatible services (MinIO, Ceph, etc.) or private endpoints.</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">s3.region = "eu-west-1"          # AWS region (optional — auto-discovered if omitted)
s3.url = "https://minio.example.com:9000"  # custom endpoint (S3-compatible)
s3.scheme = "https"              # http | https  (default: http, ignored when url is set)
s3.port = "443"                  # port (default: 80, ignored when url is set)</pre>
          <div class="alert alert-success py-1 px-2 mb-1 small d-flex align-items-start gap-2">
            <i class="bi bi-lightbulb flex-shrink-0 mt-1"></i>
            <div><strong>Region is optional.</strong> If <code>s3.region</code> is not set and
            <code>s3.bucketName</code> is configured, the module automatically discovers the bucket's
            region at connect time using <code>GetBucketLocation</code>. The discovered region is logged
            at <code>INFO</code> level. If discovery fails, <code>us-east-1</code> is used as a fallback.</div>
          </div>
          <div class="alert alert-light border py-1 px-2 mb-0 small">
            When <code>s3.url</code> is set, <code>s3.scheme</code> and <code>s3.port</code>
            are ignored. The URL is used as-is as the endpoint override.
          </div>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-shield-check text-success me-1"></i>SSL / TLS</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">s3.sslValidation = "yes"   # validate server certificate (default: no - disabled)
s3.strict = "yes"          # strict hostname verification (default: no)
s3.protocol = "TLS"        # SSL context protocol: TLS | TLSv1.2 | TLSv1.3 (default: TLS)</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-hdd-network text-secondary me-1"></i>Advanced connectivity</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">s3.listenAddress = "192.168.1.10"  # local IP to bind outgoing connections to
s3.dualstack = "yes"               # use IPv4/IPv6 dual-stack endpoint (default: no)
s3.acceleration = "yes"            # S3 Transfer Acceleration (default: no)
                                   # Note: acceleration ignores s3.url</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-arrow-repeat text-info me-1"></i>Cross-region access</p>
<pre class="bg-body-secondary rounded p-2 mb-1" style="font-size:0.75rem;white-space:pre-wrap">s3.crossRegionAccess = "yes"   # enable cross-region redirect following (default: no)
                               # When enabled: the SDK follows 301 redirects to the
                               # bucket's actual region transparently.
                               # Region is still auto-discovered at connect time
                               # (same as when s3.region is omitted).</pre>
          <div class="alert alert-light border py-1 px-2 mb-1 small">
            Region auto-discovery (via <code>GetBucketLocation</code>) is independent of
            <code>s3.crossRegionAccess</code> and runs whenever <code>s3.region</code> is blank and
            <code>s3.bucketName</code> is set. <code>s3.crossRegionAccess</code> additionally tells
            the SDK to follow cross-region 301 redirects at the HTTP layer.
          </div>
          <div class="alert alert-warning py-1 px-2 mb-0 small d-flex align-items-start gap-2">
            <i class="bi bi-exclamation-triangle flex-shrink-0 mt-1"></i>
            <div><code>s3.crossRegionAccess</code> is incompatible with <code>s3.url</code> (custom endpoint).
            Use it only with standard AWS endpoints.</div>
          </div>
        </div>

      </div><%-- end connection tab --%>


      <%-- ================================================================
           TAB 2: AUTHENTICATION & IAM
           ================================================================ --%>
      <div class="tab-pane fade" id="s3gt-auth" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1">
            <i class="bi bi-key text-warning me-1"></i>1. Static credentials (Access Key / Secret Key)
          </p>
          <p class="small text-muted mb-1">Set <strong>Login</strong> = Access Key ID and
          <strong>Password</strong> = Secret Access Key in the host <em>Identity</em> card.
          No additional properties are required.</p>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1">
            <i class="bi bi-person-lock text-secondary me-1"></i>2. IAM Role assumption (STS AssumeRole)
          </p>
          <p class="small text-muted mb-1">Useful when the access key belongs to an IAM user or role that is
          allowed to assume a more privileged role. The module calls STS <code>AssumeRole</code> before
          connecting to S3.</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">s3.roleArn = "arn:aws:iam::123456789012:role/MyS3Role"
s3.roleSessionName = "ecpds-session"   # session name tag (default: none)
s3.durationSeconds = "3600"            # STS session lifetime in seconds (default: 3600)
s3.externalId = "my-external-id"       # ExternalId condition (if required by the role trust policy)</pre>
          <div class="alert alert-light border py-1 px-2 mb-0 small">
            The STS call always uses the global <code>us-east-1</code> endpoint, so <code>s3.region</code>
            does not need to be set solely for role assumption. The base credentials (Login / Password)
            must have <code>sts:AssumeRole</code> permission on the target role ARN.
          </div>
        </div>

      </div><%-- end auth tab --%>


      <%-- ================================================================
           TAB 3: BUCKET & OBJECTS
           ================================================================ --%>
      <div class="tab-pane fade" id="s3gt-bucket" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-bucket text-primary me-1"></i>Bucket name &amp; key prefix</p>
          <p class="small text-muted mb-1">The bucket name can be set here or derived from the host
          <em>Directory</em> field (first path segment). The prefix is prepended to every object key.</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">s3.bucketName = "my-data-bucket"  # fixed bucket name (overrides the Directory field)
s3.prefix = "incoming/data/"      # key prefix for all objects (default: empty)
s3.allowEmptyBucketName = "yes"   # allow connecting without a bucket (default: no)</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-folder2-open text-secondary me-1"></i>Listing &amp; path style</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">s3.recursiveLevel = "0"            # listing depth: 0 = flat (default), -1 = unlimited, N = N levels
s3.enablePathStyleAccess = "yes"   # use path-style URLs: https://endpoint/bucket/key
                                   # (required for MinIO, Ceph and most S3-compatible services)</pre>
          <div class="alert alert-light border py-1 px-2 mb-0 small">
            Standard AWS uses virtual-hosted style (<code>bucket.s3.amazonaws.com</code>).
            Path style is mandatory for most non-AWS S3-compatible services.
          </div>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-folder-plus text-success me-1"></i>Bucket creation</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">s3.mkBucket = "yes"   # create the bucket automatically if it does not exist (default: no)</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-person text-muted me-1"></i>Listing ownership metadata</p>
          <p class="small text-muted mb-1">These values appear in the FTP-style directory listing returned
          to the acquisition engine. They do not affect S3 object ownership.</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">s3.ftpuser = "myuser"    # owner name shown in directory listings (default: login name)
s3.ftpgroup = "mygroup"  # group name shown in directory listings (default: login name)</pre>
        </div>

      </div><%-- end bucket tab --%>


      <%-- ================================================================
           TAB 4: TRANSFER TUNING
           ================================================================ --%>
      <div class="tab-pane fade" id="s3gt-transfer" role="tabpanel">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-layers text-primary me-1"></i>Multipart uploads</p>
          <p class="small text-muted mb-1">Large objects are automatically split into parts and uploaded
          in parallel. Tune thresholds and part sizes to match your network and S3 service limits.</p>
<pre class="bg-body-secondary rounded p-2 mb-2" style="font-size:0.75rem;white-space:pre-wrap">s3.partSize = "10"              # part size in MB for multipart uploads (default: 10)
s3.multipartSize = "5GB"        # threshold above which multipart is used (default: disabled/MAX)
                                # Example: "100MB" triggers multipart for files over 100 MB
s3.singlepartSize = "9223372036854775807"  # max size for single-part streaming (default: Long.MAX)
                                           # Lower this to force in-memory buffering for small files</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-memory text-secondary me-1"></i>Memory buffering</p>
<pre class="bg-body-secondary rounded p-2 mb-1" style="font-size:0.75rem;white-space:pre-wrap">s3.useByteArrayInputStream = "yes"  # buffer the object in memory before upload (default: no)
                                    # Useful when the stream size is unknown; enables retries.
                                    # Only applied when file size &lt; s3.singlepartSize.</pre>
          <div class="alert alert-warning py-1 px-2 mb-0 small d-flex align-items-start gap-2">
            <i class="bi bi-exclamation-triangle flex-shrink-0 mt-1"></i>
            <div>Enabling <code>s3.useByteArrayInputStream</code> for large files may exhaust heap memory.
            Use in combination with a low <code>s3.singlepartSize</code>.</div>
          </div>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-check2-circle text-success me-1"></i>Checksums</p>
          <p class="small text-muted mb-1">AWS SDK v2 calculates and validates checksums by default.
          Override if your S3-compatible service rejects checksum headers.</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.75rem;white-space:pre-wrap">s3.requestChecksumCalculation = "WHEN_REQUIRED"   # WHEN_SUPPORTED | WHEN_REQUIRED
s3.responseChecksumValidation = "WHEN_REQUIRED"   # WHEN_SUPPORTED | WHEN_REQUIRED
# Leave unset to use the SDK default (WHEN_SUPPORTED)</pre>
        </div>

        <hr class="my-2">

        <div class="mb-3">
          <p class="small fw-semibold mb-1"><i class="bi bi-code-slash text-muted me-1"></i>Chunked encoding</p>
<pre class="bg-body-secondary rounded p-2 mb-1" style="font-size:0.75rem;white-space:pre-wrap">s3.disableChunkedEncoding = "yes"  # disable HTTP chunked transfer encoding (default: no)
                                   # Required by some S3-compatible services that do not
                                   # support chunked encoding (e.g. older MinIO versions).</pre>
        </div>

      </div><%-- end transfer tab --%>

    </div><%-- end tab-content --%>

    <div class="mt-3 p-2 border rounded small">
      <p class="fw-semibold mb-1"><i class="bi bi-info-circle text-info me-1"></i>Typical setups</p>
      <div class="row g-2">
        <div class="col-12 col-md-6">
        <p class="small fw-semibold mb-1">Standard AWS S3 (minimal)</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.72rem;white-space:pre-wrap">s3.bucketName = "my-bucket"
s3.sslValidation = "yes"
# Login = Access Key ID, Password = Secret Key
# Region auto-discovered at connect time</pre>
      </div>
      <div class="col-12 col-md-6">
        <p class="small fw-semibold mb-1">MinIO / S3-compatible</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.72rem;white-space:pre-wrap">s3.url = "https://minio.example.com"
s3.region = "us-east-1"
s3.enablePathStyleAccess = "yes"
s3.bucketName = "my-bucket"
s3.sslValidation = "yes"</pre>
      </div>
      <div class="col-12 col-md-6 mt-2">
        <p class="small fw-semibold mb-1">IAM Role assumption</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.72rem;white-space:pre-wrap">s3.bucketName = "my-bucket"
s3.roleArn = "arn:aws:iam::123:role/R"
s3.roleSessionName = "ecpds"
s3.externalId = "my-external-id"
s3.sslValidation = "yes"
# s3.region omitted — auto-discovered</pre>
      </div>
      <div class="col-12 col-md-6 mt-2">
        <p class="small fw-semibold mb-1">Large multipart + role</p>
<pre class="bg-body-secondary rounded p-2 mb-0" style="font-size:0.72rem;white-space:pre-wrap">s3.bucketName = "my-weather-bucket"
s3.roleArn = "arn:aws:iam::123:role/R"
s3.roleSessionName = "ECPDS-Session"
s3.externalId = "my-external-id"
s3.multipartSize = "5000000000"
s3.partSize = "25"
s3.sslValidation = "yes"</pre>
      </div>
      </div>
    </div>

  </div><%-- end offcanvas-body --%>
</div><%-- end offcanvas --%>
