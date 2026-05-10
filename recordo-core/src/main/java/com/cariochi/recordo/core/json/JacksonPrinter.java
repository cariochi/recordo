package com.cariochi.recordo.core.json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;

import static tools.jackson.core.util.Separators.Spacing.BOTH;

public class JacksonPrinter extends DefaultPrettyPrinter {

    public JacksonPrinter() {
        super(Separators.createDefaultInstance().withObjectNameValueSpacing(BOTH));
        indentArraysWith(new DefaultIndenter());
        indentObjectsWith(new DefaultIndenter());
    }

    @Override
    public DefaultPrettyPrinter createInstance() {
        return new JacksonPrinter();
    }

    @Override
    public void writeEndArray(JsonGenerator g, int nrOfValues) throws JacksonException {
        if (!_arrayIndenter.isInline()) {
            --_nesting;
        }
        if (nrOfValues > 0) {
            _arrayIndenter.writeIndentation(g, _nesting);
        }
        g.writeRaw(']');
    }

    @Override
    public void writeEndObject(JsonGenerator g, int nrOfEntries) throws JacksonException {
        if (!_objectIndenter.isInline()) {
            --_nesting;
        }
        if (nrOfEntries > 0) {
            _objectIndenter.writeIndentation(g, _nesting);
        }
        g.writeRaw('}');
    }
}
