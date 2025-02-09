package com.app.backend.global.module;

import com.app.backend.global.annotation.CustomPageJsonSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.springframework.data.domain.Page;

public class CustomPageModule extends SimpleModule {

    public CustomPageModule(CustomPageJsonSerializer annotation) {
        addSerializer(Page.class, new PageSerializer(annotation));
    }

    private static class PageSerializer extends StdSerializer<Page> {

        private final CustomPageJsonSerializer annotation;

        private PageSerializer(CustomPageJsonSerializer annotation) {
            super(Page.class);
            this.annotation = annotation;
        }

        @Override
        public void serialize(Page value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            if (annotation.content())
                gen.writeObjectField("content", value.getContent());
            if (annotation.hasContent())
                gen.writeBooleanField("hasContent", value.hasContent());
            if (annotation.totalPages())
                gen.writeNumberField("totalPages", value.getTotalPages());
            if (annotation.totalElements())
                gen.writeNumberField("totalElements", value.getTotalElements());
            if (annotation.numberOfElements())
                gen.writeNumberField("numberOfElements", value.getNumberOfElements());
            if (annotation.size())
                gen.writeNumberField("size", value.getSize());
            if (annotation.number())
                gen.writeNumberField("number", value.getNumber());
            if (annotation.hasPrevious())
                gen.writeBooleanField("hasPrevious", value.hasPrevious());
            if (annotation.hasNext())
                gen.writeBooleanField("hasNext", value.hasNext());
            if (annotation.isFirst())
                gen.writeBooleanField("isFirst", value.isFirst());
            if (annotation.isLast())
                gen.writeBooleanField("isLast", value.isLast());
            if (annotation.sort())
                gen.writeObjectField("sort", value.getSort());
            if (annotation.empty())
                gen.writeBooleanField("empty", value.isEmpty());
            gen.writeEndObject();
        }

    }

}
