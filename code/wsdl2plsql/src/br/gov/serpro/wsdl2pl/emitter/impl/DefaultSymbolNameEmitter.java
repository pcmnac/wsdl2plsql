package br.gov.serpro.wsdl2pl.emitter.impl;

import br.gov.serpro.wsdl2pl.emitter.ISymbolNameEmitter;
import br.gov.serpro.wsdl2pl.type.Parameter;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;
import br.gov.serpro.wsdl2pl.util.U;

public class DefaultSymbolNameEmitter implements ISymbolNameEmitter
{

    @Override
    public String record(String namespacePrefix, String name)
    {
        return U.toUnderscored("tp_rc_" + name).toLowerCase();
    }

    @Override
    public String varray(String namespacePrefix, String name)
    {
        return U.toUnderscored("tp_va_" + name).toLowerCase();
    }

    @Override
    public String field(String namespacePrefix, String name, ITypeDef type, String recordName)
    {
        return U.toUnderscored("m_" + name).toLowerCase();
    }

    @Override
    public String function(String namespacePrefix, String name)
    {
        return U.toUnderscored("fc_" + name).toLowerCase();
    }

    @Override
    public String param(String namespacePrefix, String name, Parameter.Direction direction, boolean header, ITypeDef type, String functionName)
    {
        return U.toUnderscored("wp_" + (header ? "Header" + direction.name() + "_" : "") + name).toLowerCase();
    }

    @Override
    public String localVar(String namespacePrefix, String name, ITypeDef type, String functionName)
    {
        return U.toUnderscored("wl_" + name).toLowerCase();
    }

    // private String ns(String prefix)
    // {
    // return prefix != null ? (prefix + "_") : "";
    // }

}
