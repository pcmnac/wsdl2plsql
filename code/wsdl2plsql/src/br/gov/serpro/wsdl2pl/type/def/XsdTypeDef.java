package br.gov.serpro.wsdl2pl.type.def;

import groovy.xml.QName;
import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.util.U;

public class XsdTypeDef implements ITypeDef
{

    private QName xsdType;

    private Context context;

    public XsdTypeDef(Context context, QName xsdType)
    {
        this.context = context;
        this.xsdType = xsdType;
    }

    @Override
    public QName getXsdType()
    {
        return xsdType;
    }

    public Context getContext()
    {
        return context;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    @Override
    public String emit()
    {
        return U.baseTypeToPlType(getXsdType(), getContext());
    }

    @Override
    public String getId()
    {
        return getXsdType().toString();
    }

}
