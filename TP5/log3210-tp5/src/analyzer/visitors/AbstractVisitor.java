package analyzer.visitors;

import analyzer.ast.*;

public class AbstractVisitor implements ParserVisitor {

    public AbstractVisitor() {}


    @Override
    public Object visit(SimpleNode node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTProgram node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTNumberRegister node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTLive node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTLiveNode node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTInNode node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTOutNode node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTBlock node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTStmt node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTAssignStmt node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTAssignUnaryStmt node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTAssignDirectStmt node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTExpr node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTIdentifier node, Object data) {
        return null;
    }

    @Override
    public Object visit(ASTIntValue node, Object data) {
        return null;
    }
}
