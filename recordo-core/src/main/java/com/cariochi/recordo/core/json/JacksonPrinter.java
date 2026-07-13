package com.cariochi.recordo.core.json;

import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;

import static tools.jackson.core.util.Separators.Spacing.AFTER;

public class JacksonPrinter extends DefaultPrettyPrinter {

    public JacksonPrinter() {
        super(Separators.createDefaultInstance()
                .withObjectNameValueSpacing(AFTER)
                .withObjectEmptySeparator("")
                .withArrayEmptySeparator(""));
        indentArraysWith(new DefaultIndenter());
    }

    @Override
    public DefaultPrettyPrinter createInstance() {
        return new JacksonPrinter();
    }
}
