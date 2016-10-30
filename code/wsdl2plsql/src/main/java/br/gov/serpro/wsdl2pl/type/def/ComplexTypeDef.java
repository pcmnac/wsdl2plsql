package br.gov.serpro.wsdl2pl.type.def;

import groovy.xml.QName;
import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.util.U;

public class ComplexTypeDef extends XsdTypeDef
{
    public ComplexTypeDef(Context context, QName xsdType)
    {
        super(context, xsdType);
    }

    @Override
    public String emit()
    {
        return U.toPlIdentifier(getContext().getSymbolNameEmitter().record(getXsdType().getPrefix(),
                getXsdType().getLocalPart()));
    }

    @Override
    public String getId()
    {
        return super.getId();
    }

}
