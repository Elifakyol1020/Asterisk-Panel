package com.netgsm.asterisk.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
@Component
public class CdrIndex {
    private final String name;
    public CdrIndex(@Value("${app.cdr.index:asterisk-cdr}") String name) {
        if (!name.matches("[a-z0-9][a-z0-9._-]{0,200}")) throw new IllegalArgumentException("Invalid CDR index");
        this.name = name;
    }
    public String getName() { return name; }
    public IndexCoordinates coordinates() { return IndexCoordinates.of(name); }
}
