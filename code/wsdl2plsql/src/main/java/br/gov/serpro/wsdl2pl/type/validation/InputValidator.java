package br.gov.serpro.wsdl2pl.type.validation;

public interface InputValidator
{
    public String emit(int indent, String varName);

    public String randomValue();

    public String errorMessage(String varName);
}
