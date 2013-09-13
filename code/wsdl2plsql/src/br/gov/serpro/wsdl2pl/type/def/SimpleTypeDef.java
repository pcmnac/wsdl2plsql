package br.gov.serpro.wsdl2pl.type.def;

import br.gov.serpro.wsdl2pl.parser.Context;

import com.predic8.schema.SimpleType;

public class SimpleTypeDef extends XsdTypeDef
{
    public SimpleTypeDef(Context context, SimpleType simpleType)
    {
        super(context, simpleType.getRestriction().getBase());
    }

}
