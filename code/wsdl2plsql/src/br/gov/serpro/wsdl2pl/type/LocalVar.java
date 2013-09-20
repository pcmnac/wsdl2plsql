package br.gov.serpro.wsdl2pl.type;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.util.U;

public class LocalVar extends Symbol
{
    private String name;
    private String type;
    private String scope;

    public LocalVar(Context context, String name, String type, String scope)
    {
        super(context);
        this.name = name;
        this.type = type;
        this.scope = scope;
    }

    public String decl()
    {
        return String.format("%s %s;", name(), type);
    }

    public String decl(String initialValue)
    {
        return String.format("%s %s := %s;", name(), type, initialValue);
    }

    public String name()
    {
        return U.toPlIdentifier(getContext().getSymbolNameEmitter().localVar("", name, null, scope), null, scope);
    }

}
