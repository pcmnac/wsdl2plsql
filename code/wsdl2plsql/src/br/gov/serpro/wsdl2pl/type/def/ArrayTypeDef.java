package br.gov.serpro.wsdl2pl.type.def;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.util.U;

public class ArrayTypeDef extends XsdTypeDef
{
    private ITypeDef baseType;

    public ArrayTypeDef(Context context, ITypeDef baseType)
    {
        super(context, baseType.getXsdType());
    }

    public ITypeDef getBaseType()
    {
        return baseType;
    }

    @Override
    public String emit()
    {
        return U.toPlIdentifier(getContext().getSymbolNameEmitter().varray(getXsdType().getPrefix(),
                getXsdType().getLocalPart()));
    }

    @Override
    public String getId()
    {
        return "array-of-" + super.getId();
    }

}
