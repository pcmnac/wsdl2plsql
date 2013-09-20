package br.gov.serpro.wsdl2pl.type;

import javax.xml.namespace.QName;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;
import br.gov.serpro.wsdl2pl.util.U;

public class Exception extends DerivedSymbol implements Identifiable<String>
{
    private ITypeDef type;

    public Exception(Context context, ElementInfo element)
    {
        super(context, element);
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
        return name() + " " + getContext().getKeywordEmitter().exception();
    }

    public String var()
    {
        return varName() + " " + getType().emit();
    }

    public String varName()
    {
        String prefix = getContext().getPrefix(getElement().getNamespace());
        String name = U.toPlIdentifier(
                getContext().getSymbolNameEmitter().exceptionVar(prefix, getElement().getName(), getType()), null);

        return name;
    }

    public String name()
    {
        String prefix = getContext().getPrefix(getElement().getNamespace());
        return U.toPlIdentifier(getContext().getSymbolNameEmitter()
                .exception(prefix, getElement().getName(), getType()), null);
    }

    public String comments()
    {
        return new QName(getElement().getNamespace(), getElement().getName(), getContext().getPrefix(
                getElement().getNamespace())).toString();
    }

    @Override
    public String getId()
    {
        return getElement().getNamespace() + "/" + getElement().getName();
    }
}
