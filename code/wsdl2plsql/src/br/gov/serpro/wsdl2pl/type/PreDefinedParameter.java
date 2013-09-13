package br.gov.serpro.wsdl2pl.type;

import br.gov.serpro.wsdl2pl.parser.Context;
import br.gov.serpro.wsdl2pl.util.U;

public class PreDefinedParameter extends Symbol
{
    private String name;
    private String type;
    private String scope;

    public PreDefinedParameter(Context context, String name, String type, String scope)
    {
        super(context);
        this.name = name;
        this.type = type;
        this.scope = scope;
    }

    public String decl()
    {
        return String.format("%s %s", name(), type);
    }

    public String name()
    {
        return U.toPlIdentifier(getContext().getSymbolNameEmitter().param("", name, Parameter.Direction.IN, false, null, scope), null, scope);
    }

}
