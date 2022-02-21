package analyzer.visitors;

import analyzer.SemantiqueError;
import analyzer.ast.*;

import javax.lang.model.element.VariableElement;
import javax.xml.crypto.Data;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created: 19-01-10
 * Last Changed: 22-01-29
 * Author: Esther Guerrier
 * Modified by: Hakim Mektoub
 * <p>
 * Description: Ce visiteur explorer l'AST est renvois des erreur lorqu'une erreur sémantique est détecté.
 */

public class SemantiqueVisitor implements ParserVisitor {

    private final PrintWriter writer;

    private HashMap<String, VarType> symbolTable = new HashMap<>(); // mapping variable -> type

    // variable pour les metrics
    private int VAR = 0;
    private int WHILE = 0;
    private int IF = 0;
    private int FOR = 0;
    private int OP = 0;
    private boolean error = false;

    public SemantiqueVisitor(PrintWriter writer) {
        this.writer = writer;
    }

    //Vous pouvez utilisez cette fonction pour imprimer vos erreurs.
    private void print(final String msg) {
        if (!error) {
            writer.print(msg);
            error = true;
        }
    }

    /*
    Le Visiteur doit lancer des erreurs lorsqu'un situation arrive.

    regardez l'énoncé ou les tests pour voir le message à afficher et dans quelle situation.
    Lorsque vous voulez afficher une erreur, utilisez la méthode print implémentée ci-dessous.
    Tous vos tests doivent passer!!

     */

    @Override
    public Object visit(SimpleNode node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTProgram node, Object data) {
        node.childrenAccept(this, data);
        print(String.format("{VAR:%d, WHILE:%d, IF:%d, FOR:%d, OP:%d}", VAR, WHILE, IF, FOR, OP));

        return null;
    }

    /*
    Ici se retrouve les noeuds servant à déclarer une variable.
    Certaines doivent enregistrer les variables avec leur type dans la table symbolique.
     */
    @Override
    public Object visit(ASTDeclaration node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTNormalDeclaration node, Object data) {
        String varName = ((ASTIdentifier) node.jjtGetChild(0)).getValue();
        String typeName = node.getValue();

        VarType type = VarType.num;

        if (typeName.equals("bool"))
            type = VarType.bool;
        else if(typeName.equals("real"))
            type = VarType.real;

        if(symbolTable.containsKey(varName))
            print("Invalid declaration... variable " + varName + " already exists");

        symbolTable.put(varName, type);
        VAR++;
        return null;
    }

    @Override
    public Object visit(ASTListDeclaration node, Object data) {
        String varName = ((ASTIdentifier) node.jjtGetChild(0)).getValue();
        String typeName = node.getValue();

        VarType type = VarType.listnum;

        if (typeName.equals("listbool"))
            type = VarType.listbool;
        else if(typeName.equals("listreal"))
            type = VarType.listreal;


        if(symbolTable.containsKey(varName))
            print("Invalid declaration... variable " + varName + " already exists");

        symbolTable.put(varName, type);
        VAR++;
        return null;
    }

    @Override
    public Object visit(ASTBlock node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }


    @Override
    public Object visit(ASTStmt node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    /*
     * Il faut vérifier que le type déclaré à gauche soit compatible avec la liste utilisée à droite. N'oubliez pas
     * de vérifier que les variables existent.
     */

    @Override
    public Object visit(ASTForEachStmt node, Object data) {

        String leftOpType = ((ASTNormalDeclaration)node.jjtGetChild(0)).getValue();
        String rightOperandName = ((ASTIdentifier)node.jjtGetChild(1)).getValue();

        if (!symbolTable.containsKey(rightOperandName))
            print("Invalid use of undefined Identifier " + rightOperandName);

        VarType rightOpType = symbolTable.get(rightOperandName);
//        VarType acceptedType ;
//
//        if(rightOpType == VarType.listnum)
//            acceptedType = VarType.num;
//        else if (rightOpType == VarType.listbool)
//            acceptedType = VarType.bool;
//        else if (rightOpType == VarType.listreal)
//            acceptedType = VarType.real;
//        else {
//            print("Array type is required here...");
//        }

        VarType leftOperandType = symbolTable.get(((ASTIdentifier) node.jjtGetChild(0).jjtGetChild(0)).getValue());

        if (leftOperandType != rightOpType)
            print("Array type " + rightOpType + " is incompatible with declared variable of type " + leftOpType + "...");
        FOR++;
        return null;
    }

    /*
    Ici faites attention!! Lisez la grammaire, c'est votre meilleur ami :)
     */
    @Override
    public Object visit(ASTForStmt node, Object data) {
        FOR++;
        node.childrenAccept(this, data);
        return null;
    }

    /*
    Méthode recommandée à implémenter puisque vous remarquerez que quelques fonctions ont exactement le même code! N'oubliez
    -pas que la qualité du code est évalué :)
     */
    private void callChildenCond(SimpleNode node) {
        DataStruct ds = new DataStruct();
        node.jjtGetChild(0).jjtAccept(this,ds);
        // for i=1 to last accept cjhild
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            DataStruct ds2 = new DataStruct();
            node.jjtGetChild(i).jjtAccept(this, ds2);
            if(ds2.type != VarType.bool)
                print("Invalid type in condition");
        }
        if(ds.type != VarType.bool)
            print("Invalid type in condition");

    }

    /*
    les structures conditionnelle doivent vérifier que leur expression de condition est de type booléenne
    On doit aussi compter les conditions dans les variables IF et WHILE
     */
    @Override
    public Object visit(ASTIfStmt node, Object data) {
        callChildenCond(node);
        IF++;
        return null;
    }

    @Override
    public Object visit(ASTWhileStmt node, Object data) {
        callChildenCond(node);
        WHILE++;
        return null;
    }

    /*
    On doit vérifier que le type de la variable est compatible avec celui de l'expression.
    La variable doit etre déclarée.
     */
    @Override
    public Object visit(ASTAssignStmt node, Object data) {
        String varName = ((ASTIdentifier) node.jjtGetChild(0)).getValue();
        VarType varType = symbolTable.get(varName);
        DataStruct ds = new DataStruct();

        node.jjtGetChild(1).jjtAccept(this, ds);
//        child.jjtAccept(this, ds);

//        String childVarName = child instanceof ASTIdentifier ? ((ASTIdentifier) child).getValue() : null;

        if (!symbolTable.containsKey(varName))
            print("Invalid use of undefined Identifier " + varName);
        if(!varType.equals(ds.type))
            print("Invalid type in assignation of Identifier " + varName + "... was expecting " + varType + " but got " + ds.type);

        return null;
    }

    @Override
    public Object visit(ASTExpr node, Object data) {
        //Il est normal que tous les noeuds jusqu'à expr retourne un type.
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTCompExpr node, Object data) {
        /*attention, ce noeud est plus complexe que les autres.
        si il n'a qu'un seul enfant, le noeud a pour type le type de son enfant.

        si il a plus d'un enfant, alors ils s'agit d'une comparaison. il a donc pour type "bool".

        de plus, il n'est pas acceptable de faire des comparaisons de booleen avec les opérateur < > <= >=.
        les opérateurs == et != peuvent être utilisé pour les nombres, les réels et les booléens, mais il faut que le type soit le même
        des deux côté de l'égalité/l'inégalité.
        */

        System.out.println("CompExpr");

        if (node.jjtGetNumChildren() == 1) {
            System.out.println(((DataStruct) data).type);
            node.childrenAccept(this, data);
            System.out.println(((DataStruct) data).type);
        }
        else {
            DataStruct dataLeft = new DataStruct();
            DataStruct dataRight = new DataStruct();

            node.jjtGetChild(0).jjtAccept(this, dataLeft);
            node.jjtGetChild(1).jjtAccept(this, dataRight);

            System.out.println(dataLeft.type);
            System.out.println(dataRight.type);

            if(dataLeft.type == VarType.bool && dataRight.type == VarType.bool) {
                if(node.getValue().equals("!=") || node.getValue().equals("==")) {

                    print("10");
//                    print("Invalid type in expression");
                }
            }

            if(dataLeft.type != dataRight.type) {
                print("11");
//                print("Invalid type in expression");
            }
            OP++;
            ((DataStruct) data).type = VarType.bool;
        }
        return null;
    }

    private void callChildren(SimpleNode node, Object data, VarType validType) {

    }

    /*
    opérateur binaire
    si il n'y a qu'un enfant, aucune vérification à faire.
    par exemple, un AddExpr peut retourné le type "Bool" à condition de n'avoir qu'un seul enfant.
    Sinon, il faut s'assurer que les types des valeurs sont les mêmes des deux cotés de l'opération
     */
    @Override
    public Object visit(ASTAddExpr node, Object data) {

        System.out.println("AddExpr");
        if (node.jjtGetNumChildren() == 1)
            node.childrenAccept(this, data);
        else {
            OP += node.getOps().size();

//        System.out.println(node.jjtGetNumChildren());
            DataStruct ds1 = new DataStruct();
            DataStruct ds2 = new DataStruct();
            node.jjtGetChild(0).jjtAccept(this, ds1);
            node.jjtGetChild(1).jjtAccept(this, ds2);

            System.out.println("DS1=" + ds1.type);
            System.out.println("DS2=" + ds2.type);

            if(ds1.type != ds2.type)
                print("3");
            else if ((ds1.type != VarType.num && ds1.type != VarType.real) || (ds2.type != VarType.num && ds2.type != VarType.real))
                print("45215");
//            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
//                node.jjtGetChild(i).jjtAccept(this, ds);
//                System.out.println(ds.type);
//                if (ds.type != VarType.num && ds.type != VarType.real)
//                    print("3");
////                print("Invalid type in expression");
//            }
            else {
                ((DataStruct) data).type = ds1.type;
            }
        }
        return null;
    }

    @Override
    public Object visit(ASTMulExpr node, Object data) {
        System.out.println("MulExpr");
//        System.out.println(node.jjtGetNumChildren());
        if (node.jjtGetNumChildren() == 1)
            node.childrenAccept(this, data);
        else {
            OP += node.getOps().size();
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                DataStruct ds = new DataStruct();
                node.jjtGetChild(i).jjtAccept(this, ds);
//            System.out.println(ds.type);
                if (ds.type != VarType.num && ds.type != VarType.real)
                    print("4");
//                print("Invalid type in expression");
            }
        }
        return null;
    }

    @Override
    public Object visit(ASTBoolExpr node, Object data) {
//        System.out.println(node.jjtGetNumChildren());
        if (node.jjtGetNumChildren() == 1)
            node.childrenAccept(this, data);
        else {
            OP += node.getOps().size();
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                DataStruct ds = new DataStruct();
                node.jjtGetChild(i).jjtAccept(this, ds);
//            System.out.println(ds.type);
                if (ds.type != VarType.bool)
                    print("5");
//                print("Invalid type in expression");
            }
        }
        return null;
    }

    /*
    opérateur unaire
    les opérateur unaire ont toujours un seul enfant.

    Cependant, ASTNotExpr et ASTUnaExpr ont la fonction "getOps()" qui retourne un vecteur contenant l'image (représentation str)
    de chaque token associé au noeud.

    Il est utile de vérifier la longueur de ce vecteur pour savoir si une opérande est présente.

    si il n'y a pas d'opérande, ne rien faire.
    si il y a une (ou plus) opérande, ils faut vérifier le type.

    */
    @Override
    public Object visit(ASTNotExpr node, Object data) {
            System.out.println("NotExpr");
        if (node.getOps().isEmpty())
            node.childrenAccept(this, data);
        else {
            OP += node.getOps().size();
            node.jjtGetChild(0).jjtAccept(this, data);
            if(((DataStruct) data).type == VarType.bool) {
                print("6");
//                print("Invalid type in expression");
            }
        }
        return null;
    }

    @Override
    public Object visit(ASTUnaExpr node, Object data) {
        System.out.println("UnaExpr");
        if(node.getOps().isEmpty())
            node.childrenAccept(this, data);
        else {
            OP += node.getOps().size();
            node.jjtGetChild(0).jjtAccept(this, data);
            if (((DataStruct) data).type != VarType.num) {
                print("7");
//            print("Invalid type in expression");
            }
        }
        return null;
    }

    /*
    les noeud ASTIdentifier aillant comme parent "GenValue" doivent vérifier leur type et vérifier leur existence.

    Ont peut envoyé une information a un enfant avec le 2e paramètre de jjtAccept ou childrenAccept.
     */
    @Override
    public Object visit(ASTGenValue node, Object data) {
        System.out.println("Gen");
        node.jjtGetChild(0).jjtAccept(this, data);
        return null;
    }


    @Override
    public Object visit(ASTBoolValue node, Object data) {
        ((DataStruct) data).type = VarType.bool;
        return null;
    }

    @Override
    public Object visit(ASTIdentifier node, Object data) {
        if (!symbolTable.containsKey(node.getValue()))
            print("Invalid use of undefined Identifier " + node.getValue());

        ((DataStruct) data).type = symbolTable.get(node.getValue());
        return null;
    }

    @Override
    public Object visit(ASTIntValue node, Object data) {
        System.out.println("Int");
        ((DataStruct) data).type = VarType.num;
        return null;
    }

    @Override
    public Object visit(ASTRealValue node, Object data) {
        ((DataStruct) data).type = VarType.real;
        return null;
    }

    //des outils pour vous simplifier la vie et vous enligner dans le travail
    public enum VarType {
        bool,
        num,
        real,
        listnum,
        listbool,
        listreal
    }

    private class DataStruct {
        public VarType type;

        public DataStruct() {
        }

        public DataStruct(VarType p_type) {
            type = p_type;
        }
    }
}
