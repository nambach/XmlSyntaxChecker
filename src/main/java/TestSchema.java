import com.sun.org.apache.xerces.internal.impl.xs.XSImplementationImpl;
import com.sun.org.apache.xerces.internal.xs.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import java.util.HashMap;
import java.util.Map;

import static com.sun.org.apache.xerces.internal.xs.XSConstants.*;

public class TestSchema {
    private static Map<Short, String> compositors = new HashMap<>();
    private static Map<Short, String> attrType = new HashMap<>();
    private static Map<Short, String> builtinTypes = new HashMap<>();

    static {
        compositors.put(XSModelGroup.COMPOSITOR_SEQUENCE, "Sequence");
        compositors.put(XSModelGroup.COMPOSITOR_CHOICE, "Choice");
        compositors.put(XSModelGroup.COMPOSITOR_ALL, "All");

        attrType.put(VC_DEFAULT, "default");
        attrType.put(VC_FIXED, "fixed");
        attrType.put(VC_NONE, "none");

        builtinTypes.put(INTEGER_DT, "integer");
        builtinTypes.put(INT_DT, "int");
        builtinTypes.put(STRING_DT, "string");
        builtinTypes.put(FLOAT_DT, "float");
    }

    public static void main(String[] args) {
        XSModel model = getSchemaModel("src/main/resources/static/xsd/crawling.xsd");
        if (model == null) {
            return;
        }

        XSObject root = model.getComponents(ELEMENT_DECLARATION).item(0);
        XSElementDeclaration declaration = (XSElementDeclaration) root;

        System.out.println("Root: <" + declaration.getName() + "> ");
        iterate("", declaration.getTypeDefinition());
    }

    private static void iterate(String indent, XSObject element) {
        XSTypeDefinition typeDefinition = (XSTypeDefinition) element;

        switch (typeDefinition.getTypeCategory()) {
            case XSTypeDefinition.SIMPLE_TYPE:
                displaySimpleType(indent, typeDefinition);
                break;
            case XSTypeDefinition.COMPLEX_TYPE:
                displayComplexType(indent, typeDefinition);
                break;
        }

        System.out.println(indent.replace(" ", "-"));
    }

    private static void displaySimpleType(String indent, XSTypeDefinition typeDefinition) {
        XSSimpleTypeDefinition simpleTypeDefinition = (XSSimpleTypeDefinition) typeDefinition;
        String type = builtinTypes.get(simpleTypeDefinition.getBuiltInKind());
        System.out.println(indent + "Simple type, derived from " + type);
    }

    private static void displayComplexType(String indent, XSTypeDefinition typeDefinition) {
        XSComplexTypeDefinition complexTypeDefinition = (XSComplexTypeDefinition) typeDefinition;

        switch (complexTypeDefinition.getContentType()) {
            case XSComplexTypeDefinition.CONTENTTYPE_EMPTY:
                System.out.println(indent + "Empty tag");
                break;
            case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE:
                System.out.println(indent + "Text-only, derived from " + complexTypeDefinition.getSimpleType().getName());
                break;
            case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
                System.out.println(indent + "Element-only");
                analyzeParticle(indent, complexTypeDefinition.getParticle());
                break;
            case XSComplexTypeDefinition.CONTENTTYPE_MIXED:
                System.out.println(indent + "Mixed");
                analyzeParticle(indent, complexTypeDefinition.getParticle());
                break;
        }
        displayAttributes(indent, complexTypeDefinition.getAttributeUses());
    }

    private static void displayAttributes(String indent, XSObjectList attrList) {
        for (int i = 0; i < attrList.getLength(); i++) {
            XSObject xsObject = attrList.item(i);
            XSAttributeUse attributeUse = (XSAttributeUse) xsObject;

            String name = attributeUse.getAttrDeclaration().getName();
            String vcType = attrType.get(attributeUse.getConstraintType());
            String type = attributeUse.getAttrDeclaration().getTypeDefinition().getName();
            String required = attributeUse.getRequired() ? "required" : "optional";
            String defaultValue = attributeUse.getConstraintValue();

            System.out.println(indent + (i + 1) + " - "
                    + name + " " + type + " " + vcType + " " + required + " " + defaultValue);
        }
    }

    private static void analyzeParticle(String indent, XSParticle particle) {
        if (particle.getTerm().getType() == MODEL_GROUP) {
            XSModelGroup modelGroup = (XSModelGroup) particle.getTerm();
            System.out.println(indent + compositors.get(modelGroup.getCompositor()));

            XSObjectList list = modelGroup.getParticles();
            for (int i = 0; i < list.getLength(); i++) {
                XSParticle particleItem = (XSParticle) list.item(i);

                String maxOccurs = particleItem.getMaxOccursUnbounded() ? "unbounded" : particleItem.getMaxOccurs() + "";

                switch (particleItem.getTerm().getType()) {
                    case ELEMENT_DECLARATION:
                        XSElementDeclaration declaration = (XSElementDeclaration) particleItem.getTerm();

                        System.out.println(indent + "  " + "Name: <" + declaration.getName() + "> " + maxOccurs);
                        iterate(indent + "  ", declaration.getTypeDefinition());
                        break;
                }
            }
        }
    }

    private static XSModel getSchemaModel(String path) {
        try {
            System.setProperty(DOMImplementationRegistry.PROPERTY, "com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl");
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();

            XSImplementationImpl impl = (XSImplementationImpl) registry.getDOMImplementation("XS-Loader");
            XSLoader schemaLoader = impl.createXSLoader(null);

            return schemaLoader.loadURI(path);
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
