package br.gov.serpro.wsdl2pl.type.def;

import br.gov.serpro.wsdl2pl.type.Identifiable;
import groovy.xml.QName;

public interface ITypeDef extends Identifiable<String>
{
    String emit();

    QName getXsdType();

    // String getPlType();

    void setRequired(boolean required);

}
