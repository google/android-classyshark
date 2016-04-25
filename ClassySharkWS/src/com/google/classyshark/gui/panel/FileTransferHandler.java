/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.classyshark.gui.panel;

import com.google.classyshark.gui.panel.io.CurrentFolderConfig;
import com.google.classyshark.gui.panel.io.RecentArchivesConfig;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.classyshark.gui.panel.io.FileChooserUtils.isSupportedArchiveFile;

public class FileTransferHandler extends TransferHandler {

    private final ArchiveDisplayer archiveDisplayer;

    public FileTransferHandler(ArchiveDisplayer archiveDisplayer) {
        this.archiveDisplayer = archiveDisplayer;
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    public boolean canImport(TransferSupport ts) {
        return ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    public boolean importData(TransferSupport ts) {
        try {
            @SuppressWarnings("rawtypes")
            List data = (List) ts.getTransferable().getTransferData(
                    DataFlavor.javaFileListFlavor);
            if (data.size() < 1) {
                return false;
            }

            for (Object item : data) {
                File file = (File) item;

                if(isSupportedArchiveFile(file)) {
                    CurrentFolderConfig.INSTANCE.setCurrentDirectory(file.getParentFile());
                    RecentArchivesConfig.INSTANCE.addArchive(file.getName(),
                            file.getParentFile());
                    archiveDisplayer.displayArchive(file);
                }
            }

            return true;

        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
