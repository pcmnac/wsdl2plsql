package br.gov.serpro.wsdl2pl.type;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;
import br.gov.serpro.wsdl2pl.util.SB;
import br.gov.serpro.wsdl2pl.util.U;

public class VarrayType extends Type
{
    private ITypeDef type;

    public VarrayType(Context context, ITypeDef type)
    {
        super(context);
        this.type = type;
    }

    public ITypeDef getType()
    {
        return type;
    }

    @Override
    public String getId()
    {
        return "array-of-" + getType().getId();
    }

    @Override
    public String decl(int indent)
    {
        SB arrayType = new SB();
        IKeywordEmitter ke = getContext().getKeywordEmitter();

        arrayType.l(indent, "%s %s %s %s(%s) %s %s;", ke.type(), getName(), ke.is(), ke.varray(), "100000", ke.of(), getType().emit());

        return arrayType.toString();
    }

    @Override
    public String forwardDecl()
    {
        return String.format("%s %s;", getContext().getKeywordEmitter().type(), getName());
    }

    @Override
    public String comments()
    {
        return "array of " + getType().getXsdType().toString();
    }

    private String getName()
    {
        return U.toPlIdentifier(getContext().getSymbolNameEmitter().varray(getType().getXsdType().getPrefix(), getType().getXsdType().getLocalPart()));
    }
}
