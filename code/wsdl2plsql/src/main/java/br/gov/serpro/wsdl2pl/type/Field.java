package br.gov.serpro.wsdl2pl.type;

import javax.xml.namespace.QName;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;
import br.gov.serpro.wsdl2pl.util.U;

public class Field extends DerivedSymbol
{
    private ITypeDef type;
    private RecordType owner;

    public Field(Context context, RecordType owner, ElementInfo element)
    {
        super(context, element);
        this.owner = owner;
    }

    public RecordType getOwner()
    {
        return owner;
    }

    public void setOwner(RecordType owner)
    {
        this.owner = owner;
    }

    public ITypeDef getType()
    {
        return type;
    }

    public void setType(ITypeDef type)
    {
        this.type = type;
    }

    public String decl()
    {
        return name() + " " + getType().emit();
    }

    public String name()
    {
        String prefix = getContext().getPrefix(getElement().getNamespace());
        return U.toPlIdentifier(getContext().getSymbolNameEmitter().field(prefix, getElement().getName(), getType(), getOwner().getId()), null,
                getOwner().getId());
    }

    public String comments()
    {
        return new QName(getElement().getNamespace(), getElement().getName(), getContext().getPrefix(getElement().getNamespace())).toString();
    }
}
