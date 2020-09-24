package com.cariochi.recordo.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;

import java.io.IOException;

public class JacksonPrinter extends DefaultPrettyPrinter {

    public JacksonPrinter() {
        super(
                new DefaultPrettyPrinter()
                        .withArrayIndenter(new DefaultIndenter())
                        .withObjectIndenter(new DefaultIndenter())
                        .withSpacesInObjectEntries()
        );
        withSeparators(_separators);
    }

    @Override
    public DefaultPrettyPrinter createInstance() {
        return new JacksonPrinter();
    }

    @Override
    public DefaultPrettyPrinter withSeparators(Separators separators) {
        _separators = separators;
        _objectFieldValueSeparatorWithSpaces = separators.getObjectFieldValueSeparator() + " ";
        return this;
    }

    @Override
    public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
        if (!_arrayIndenter.isInline()) {
            --_nesting;
        }
        if (nrOfValues > 0) {
            _arrayIndenter.writeIndentation(g, _nesting);
        }
        g.writeRaw(']');
    }

    @Override
    public void writeEndObject(JsonGenerator g, int nrOfEntries) throws IOException {
        if (!_objectIndenter.isInline()) {
            --_nesting;
        }
        if (nrOfEntries > 0) {
            _objectIndenter.writeIndentation(g, _nesting);
        }
        g.writeRaw('}');
    }
}
