package br.gov.serpro.wsdl2pl.writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;
import br.gov.serpro.wsdl2pl.type.Exception;
import br.gov.serpro.wsdl2pl.type.Field;
import br.gov.serpro.wsdl2pl.type.Function;
import br.gov.serpro.wsdl2pl.type.LocalVar;
import br.gov.serpro.wsdl2pl.type.Parameter;
import br.gov.serpro.wsdl2pl.type.RecordType;
import br.gov.serpro.wsdl2pl.type.VarrayType;
import br.gov.serpro.wsdl2pl.type.def.ArrayTypeDef;
import br.gov.serpro.wsdl2pl.type.def.ComplexTypeDef;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;
import br.gov.serpro.wsdl2pl.util.SB;
import br.gov.serpro.wsdl2pl.util.U;

public class TestsWriter extends BaseWriter
{
    public TestsWriter(Context context)
    {
        super(context);
    }

    public String write()
    {
        SB tests = new SB();
        IKeywordEmitter ke = getContext().getKeywordEmitter();
        int l = 1;
        String pkg = getContext().getPackageName();

        // BEGIN
        tests.l(0, ke.begin());

        // BEGIN
        tests.l();
        for (Function function : getContext().getPlFunctions())
        {
            // result
            LocalVar varResult = new LocalVar(getContext(), "result", pkg + "." + function.getReturnType().emit(),
                    function.getId());

            List<LocalVar> params = new ArrayList<LocalVar>();
            // params
            for (int i = 0; i < function.getParameters().size(); i++)
            {
                Parameter parameter = function.getParameters().get(i);
                params.add(new LocalVar(getContext(), parameter.getElement().getName(), pkg + "."
                        + parameter.getType().emit(), function.getId()));
            }

            if (function.getInputHeader() != null)
            {
                params.add(new LocalVar(getContext(), function.getInputHeader().getElement().getName(), pkg + "."
                        + function.getInputHeader().getType().emit(), function.getId()));
            }

            if (function.getOutputHeader() != null)
            {
                params.add(new LocalVar(getContext(), function.getOutputHeader().getElement().getName(), pkg + "."
                        + function.getOutputHeader().getType().emit(), function.getId()));
            }

            tests.l(l, "-- test skeleton for: %s", function.name());
            // DECLARE
            tests.l(l, ke.declare());
            tests.l();
            // vars
            tests.l(l + 1, varResult.decl());
            for (LocalVar param : params)
            {
                tests.l(l + 1, param.decl());
            }
            tests.l();

            // BEGIN
            tests.l(l, ke.begin());

            // fill params
            tests.l();
            tests.l(l + 1, "-- fill input parameters");

            boolean inputHeaderFound = false;
            for (int i = 0; i < params.size(); i++)
            {
                LocalVar param = params.get(i);

                Parameter parameter = null;
                if (i < function.getParameters().size())
                {
                    parameter = function.getParameters().get(i);
                }
                else
                {
                    if (function.getInputHeader() != null && !inputHeaderFound)
                    {
                        parameter = function.getInputHeader();
                        inputHeaderFound = true;
                    }
                    else
                    {
                        parameter = function.getOutputHeader();
                    }
                }

                if (parameter.getDirection().equals(Parameter.Direction.IN))
                {
                    tests.l(fill(param.name(), parameter.getType(), l + 1));
                }
            }

            // call
            tests.l(l + 1, "dbms_output.put_line('---------------------------');");
            tests.l(l + 1, "dbms_output.put_line('Invoking %s()');", function.name());

            tests.a(l + 1, "%s := %s.%s(", varResult.name(), pkg, function.name());
            for (int i = 0; i < params.size(); i++)
            {
                LocalVar param = params.get(i);
                tests.a(param.name());
                if (i < params.size() - 1)
                {
                    tests.a(", ");
                }
            }
            tests.l(");\n");

            tests.l(dump(varResult.name(), function.getReturnType(), l + 1, 0));

            if (function.getOutputHeader() != null)
            {
                LocalVar param = params.get(params.size() - 1);
                tests.l(dump(param.name(), function.getOutputHeader().getType(), l + 1, 0));
            }

            // EXCEPTION
            tests.l(l, ke.exception());
            tests.l();

            for (Exception exception : function.getExceptions())
            {
                // WHEN exception THEN
                tests.l(l + 1, "%s %s.%s %s\n", ke.when(), pkg, exception.name(), ke.then());
                tests.l(l + 2, "dbms_output.put_line('%s');", exception.name());
                tests.l(dump(pkg + "." + getContext().getSoapFaultException().varName(), getContext()
                        .getSoapFaultException().getType(), l + 2, 0));
                tests.l(dump(pkg + "." + exception.varName(), exception.getType(), l + 2, 0));
            }

            // WHEN soap fault THEN
            tests.l(l + 1, "%s %s.%s %s\n", ke.when(), pkg, getContext().getSoapFaultException().name(), ke.then());
            tests.l(l + 2, "dbms_output.put_line('%s');", getContext().getSoapFaultException().name());
            tests.l(dump(pkg + "." + getContext().getSoapFaultException().varName(), getContext()
                    .getSoapFaultException().getType(), l + 2, 0));

            // WHEN OTHERS THEN
            tests.l(l + 1, "%s %s %s\n", ke.when(), ke.others(), ke.then());
            tests.l(l + 2, "dbms_output.put_line('OTHERS: ' || sqlcode || ' - ' || sqlerrm);");
            // END
            tests.l(l, "%s;", ke.end());
            tests.l();
        }

        // END
        tests.l(0, "%s;", ke.end());
        tests.l();

        return tests.toString();
    }

    private String fill(String var, ITypeDef type, int depth)
    {
        SB block = new SB();
        IKeywordEmitter ke = getContext().getKeywordEmitter();

        if (type instanceof ComplexTypeDef)
        {
            RecordType recordType = (RecordType) getContext().getComplexTypeMap().get(type.getId());

            for (Field field : recordType.getMembers())
            {
                String fieldName = field.name();
                block.a(fill(var + "." + fieldName, field.getType(), depth));
            }

        }
        else if (type instanceof ArrayTypeDef)
        {
            VarrayType varrayType = (VarrayType) getContext().getComplexTypeMap().get(type.getId());

            String loopVarName = "i" + depth;
            // FOR i IN 1..3 LOOP
            block.l(depth, "%s %s %s 1..3 %s", ke.forKey(), loopVarName, ke.in(), ke.loop());

            block.l(depth, "%s.%s(1);", var + "(" + loopVarName + ")", ke.extend());
            block.l(fill(var + "(" + loopVarName + ")", varrayType.getType(), depth + 1));

            block.l(depth, "%s %s;", ke.end(), ke.loop());
        }
        else
        {
            block.l(depth, "%s := %s;", var, generateValue(type.getXsdType().getLocalPart()));
        }

        return block.toString();
    }

    private String dump(String var, ITypeDef type, int indent, int depth)
    {
        SB block = new SB();
        IKeywordEmitter ke = getContext().getKeywordEmitter();

        String varName = var.indexOf('.') != -1 ? var.substring(var.lastIndexOf('.') + 1) : var;

        if (type instanceof ComplexTypeDef)
        {
            RecordType recordType = (RecordType) getContext().getComplexTypeMap().get(type.getId());

            block.l(indent, "dbms_output.put_line('%s%s:');", indent(depth), varName);
            for (Field field : recordType.getMembers())
            {
                String fieldName = field.name();
                block.a(dump(var + "." + fieldName, field.getType(), indent, depth + 1));
            }

        }
        else if (type instanceof ArrayTypeDef)
        {
            VarrayType varrayType = (VarrayType) getContext().getComplexTypeMap().get(type.getId());

            String loopVarName = "i" + indent;

            block.l(indent, "dbms_output.put_line('%s%s (varray):');", indent(depth), varName);
            // FOR i IN 1..varray.COUNT LOOP
            block.l(indent, "%s %s %s 1..%s.%s %s", ke.forKey(), loopVarName, ke.in(), var, ke.count(), ke.loop());
            block.l();
            block.l(dump(var + "(" + loopVarName + ")", varrayType.getType(), indent + 1, depth + 1));

            block.l(indent, "%s %s;", ke.end(), ke.loop());
        }
        else
        {

            block.l(indent, "dbms_output.put_line('%s%s: ' || %s);", indent(depth), varName,
                    U.baseTypeToString(type.getXsdType().getLocalPart(), var));
        }

        return block.toString();
    }

    private String indent(int depth)
    {
        String result = "";

        for (int i = 0; i < depth; i++)
        {
            result += "    ";
        }

        return result;
    }

    private String generateValue(String xsdType)
    {

        String result = null;

        String[] stringTypes = { "string", "ENTITIES", "ENTITY", "ID", "IDREF", "IDREFS", "language", "Name", "NCName",
                "NMTOKEN", "NMTOKENS", "normalizedString", "QName", "token" };

        String[] longStringTypes = { "base64Binary" };

        String[] dateTypes = { "date", "dateTime", "time" };

        String[] numberTypes = { "decimal", "double", "float" };

        String[] integerTypes = { "byte", "int", "integer", "long", "negativeInteger", "nonNegativeInteger",
                "nonPositiveInteger", "positiveInteger", "short", "unsignedLong", "unsignedInt", "unsignedShort",
                "unsignedByte" };

        if (Arrays.asList(stringTypes).contains(xsdType))
        {
            result = "'abcdef'";
        }
        else if (Arrays.asList(longStringTypes).contains(xsdType))
        {
            result = "'abcdef abcdef abcdef abcdef abcdef abcdef abcdef abcdef abcdef abcdef abcdef abcdef abcdef abcdef abcdef abcdef abcdef'";
        }
        else if (Arrays.asList(dateTypes).contains(xsdType))
        {
            result = "'2013-01-01'";
        }
        else if (Arrays.asList(numberTypes).contains(xsdType))
        {
            result = "1234567890";
        }
        else if (Arrays.asList(integerTypes).contains(xsdType))
        {
            result = "1234567890";
        }
        else if ("boolean".equals(xsdType))
        {
            result = "true";
        }
        else
        {
            throw new RuntimeException("Unsupported type: " + xsdType);
        }

        return result;
    }

}
