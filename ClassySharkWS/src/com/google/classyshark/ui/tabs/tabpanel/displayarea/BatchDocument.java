/*
 * Copyright 2015 Google, Inc.
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

package com.google.classyshark.ui.tabs.tabpanel.displayarea;

import java.util.ArrayList;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;

class BatchDocument extends DefaultStyledDocument {

    private static final char[] EOL_ARRAY = { '\n' };
    private ArrayList batch = null;

    public BatchDocument() {
        batch = new ArrayList();
    }

    public void appendBatchStringNoLineFeed(String str,
                                            AttributeSet a) {
        a = a.copyAttributes();
        char[] chars = str.toCharArray();
        batch.add(new ElementSpec(
                a, ElementSpec.ContentType, chars, 0, str.length()));
    }

    public void appendBatchLineFeed(AttributeSet a) {
        batch.add(new ElementSpec(
                a, ElementSpec.ContentType, EOL_ARRAY, 0, 1));

        Element paragraph = getParagraphElement(0);
        AttributeSet pattern = paragraph.getAttributes();
        batch.add(new ElementSpec(null, ElementSpec.EndTagType));
        batch.add(new ElementSpec(pattern, ElementSpec.StartTagType));
    }

    public void processBatchUpdates(int offs) throws
            BadLocationException {
        ElementSpec[] inserts = new ElementSpec[batch.size()];
        batch.toArray(inserts);

        super.insert(offs, inserts);
    }
}