/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.ecpds.mover;

import static ecmwf.ecpds.master.DataFilePath.getDir;

import java.util.ArrayList;
import java.util.List;

import ecmwf.common.database.ExistingStorageDirectory;
import ecmwf.common.technical.GenericFile;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;

/**
 * The Class PurgeThread. This Thread is used to purge the file system (e.g. remove expired directories from the
 * repository).
 */
final class PurgeThread extends ConfigurableRunnable {
    private final MoverServer _mover;

    /** The directoriesList. */
    final List<ExistingStorageDirectory> directoriesList = new ArrayList<>();

    /** The shouldContinue. */
    boolean shouldContinue = true;

    /** The force. */
    final boolean force;

    /**
     * Instantiates a new purge thread.
     *
     * @param directories
     *            the directories
     */
    PurgeThread(final MoverServer _mover, final List<ExistingStorageDirectory> directories) {
        this._mover = _mover;
        super();

        directoriesList.addAll(directories);
        force = false;
    }

    /**
     * Instantiates a new purge thread.
     */
    PurgeThread(final MoverServer _mover) {
        this._mover = _mover;
        super();

        force = true;
    }

    /**
     * worthChecking.
     *
     * @param dir
     *            the dir
     * @param listCount
     *            the list count
     * @param listSize
     *            the list size
     *
     * @return true, if successful
     */
    private boolean worthChecking(final String dir, final long listCount, final long listSize) {
        for (final ExistingStorageDirectory directory : directoriesList) {
            if (dir.equals(getDir(directory.getFileSystem(), directory.getArrivedTime()))) {
                final long count = directory.getFilesCount();
                final var size = directory.getFilesSize();
                if (count < listCount || size < listSize) {
                    // There is a difference so we have to process the directories to check if files
                    // can be removed to recover some disk space?
                    MoverServer._log.info("Spotted a difference in count/size for dir: {} (count:{}/{}, size:{}/{})",
                            dir, count, listCount, size, listSize);
                    return true;
                }
                // Same number of files and size so we do nothing!
                return false;
            }
        }
        return true;
    }

    /**
     * Stop.
     */
    public void stop() {
        shouldContinue = false;
    }

    /**
     * Configurable run.
     */
    @Override
    public void configurableRun() {
        try {
            final var repository = GenericFile.getGenericFile(MoverServer.getRepository());
            final var volumes = repository.listFiles();
            if (volumes == null) {
                // The repository is not a directory?
                MoverServer._log.warn("Repository {} not a directory?", repository.getAbsolutePath());
                return;
            }
            final List<String> alreadyProcessed = new ArrayList<>();
            for (final GenericFile dataVolume : volumes) {
                final var realPath = dataVolume.getFile().toPath().toRealPath().toString();
                if (alreadyProcessed.contains(realPath)) {
                    // This volume was already processed!
                    MoverServer._log.debug("Skipping dataVolume {} (already processed - symbolic link?)",
                            dataVolume.getAbsolutePath());
                    continue;
                }
                alreadyProcessed.add(realPath);
                final var times = dataVolume.listFiles();
                if (times == null) {
                    // This volume is not a directory?
                    MoverServer._log.debug("Skipping dataVolume {} (not a directory)", dataVolume.getAbsolutePath());
                    continue;
                }
                for (final GenericFile arrivedTime : times) {
                    if (force || worthChecking(dataVolume.getName() + "/" + arrivedTime.getName(),
                            arrivedTime.listCount(), arrivedTime.listSize())) {
                        // We can scan this directory!
                        final var steps = arrivedTime.listFiles();
                        var stepsDeleted = 0;
                        if (steps == null) {
                            // This is not a directory?
                            MoverServer._log.debug("Skipping arrivedTime {} (not a directory)",
                                    arrivedTime.getAbsolutePath());
                            continue;
                        }
                        for (final GenericFile dataStep : steps) {
                            // Let's scan this directory to look for
                            // DataFiles!
                            final var files = dataStep.listFiles();
                            var filesDeleted = 0;
                            if (files == null) {
                                // This is not a directory?
                                MoverServer._log.debug("Skipping dataStep {} (not a directory)",
                                        dataStep.getAbsolutePath());
                                continue;
                            }
                            for (final GenericFile dataFile : files) {
                                if (!shouldContinue) {
                                    // We are asked to stop the
                                    // activity!
                                    MoverServer._log.debug("Stopping purge");
                                    return;
                                }
                                // Let's process this DataFile!
                                final var name = dataFile.getName();
                                final var path = dataFile.getAbsolutePath();
                                try {
                                    if (dataFile.isFile() && name.length() > 10 && _mover.getMasterProxy()
                                            .isValidDataFile(Long.parseLong(name.substring(0, 10)))) {
                                        // This is a valid DataFile!
                                        MoverServer._log.debug("DataFile {} is still valid (keep it)", path);
                                        continue;
                                    }
                                } catch (final NumberFormatException e) {
                                    // This is not a DataFile (bad name format)!
                                    MoverServer._log.warn("File {} not a DataFile (please remove it from repository)",
                                            path);
                                    continue;
                                }
                                // This is not a valid DataFile!
                                final var deleted = dataFile.delete();
                                MoverServer._log.debug("Deleting file {} (not a valid DataFile): {}", path, deleted);
                                if (deleted) {
                                    filesDeleted++;
                                }
                            }
                            // Let's remove the dataStep directory if all
                            // the files have been deleted!
                            if (filesDeleted == files.length) {
                                final var deleted = dataStep.delete(true);
                                MoverServer._log.info("Removed empty directory: {}: {}", dataStep.getAbsolutePath(),
                                        deleted);
                                stepsDeleted++;
                            }
                        }
                        // Let's remove the arrivedTime directory if all
                        // the steps have been deleted!
                        if (stepsDeleted == steps.length) {
                            final var deleted = arrivedTime.delete(true);
                            MoverServer._log.info("Removed empty directory: {}: {}", arrivedTime.getAbsolutePath(),
                                    deleted);
                        }
                    } else {
                        // The file exists and should not be deleted for
                        // now!
                        MoverServer._log.debug("Skipping arrivedTime {} (not expired)", arrivedTime.getAbsolutePath());
                    }
                }
            }
        } catch (final Throwable t) {
            MoverServer._log.warn("configurableRun", t);
        }
    }
}
