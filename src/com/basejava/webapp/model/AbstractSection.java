package com.basejava.webapp.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;

@XmlSeeAlso({TextSection.class, ListSection.class, OrganizationSection.class})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractSection implements Serializable {
    protected AbstractSection() {}
}
