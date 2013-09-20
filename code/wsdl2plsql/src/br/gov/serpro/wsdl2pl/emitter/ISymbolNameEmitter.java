package br.gov.serpro.wsdl2pl.emitter;

import br.gov.serpro.wsdl2pl.type.Parameter;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;

public interface ISymbolNameEmitter
{
    String record(String namespacePrefix, String name);

    String varray(String namespacePrefix, String name);

    String field(String namespacePrefix, String name, ITypeDef type, String recordName);
    
    String exception(String namespacePrefix, String name, ITypeDef type);

    String function(String namespacePrefix, String name);

    String param(String namespacePrefix, String name, Parameter.Direction direction, boolean header, ITypeDef type, String functionName);

    String localVar(String namespacePrefix, String name, ITypeDef type, String functionName);

    String exceptionVar(String prefix, String name, ITypeDef type);
}
