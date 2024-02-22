/*
* Copyright (C) 2007-2010 Júlio Vilmar Gesser.
* Copyright (C) 2011, 2013-2024 The JavaParser Team.
*
* This file is part of JavaParser.
*
* JavaParser can be used either under the terms of
* a) the GNU Lesser General Public License as published by
*     the Free Software Foundation, either version 3 of the License, or
*     (at your option) any later version.
* b) the terms of the Apache License
*
* You should have received a copy of both licenses in LICENCE.LGPL and
* LICENCE.APACHE. Please refer to those files for details.
*
* JavaParser is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*/
package com.github.javaparser.printer.lexicalpreservation;


import java.util.*;


import com.github.javaparser.GeneratedJavaParserConstants;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.ast.observer.ObservableProperty;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.printer.ConcreteSyntaxModel;
import com.github.javaparser.printer.SourcePrinter;
import com.github.javaparser.printer.Stringable;
import com.github.javaparser.printer.concretesyntaxmodel.*;
import com.github.javaparser.printer.lexicalpreservation.changes.*;
import com.github.javaparser.utils.LineSeparator;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


class LexicalDifferenceCalculator {


   static int[] flags = new int[51];


   /**
    * The ConcreteSyntaxModel represents the general format. This model is a calculated version of the ConcreteSyntaxModel,
    * with no condition, no lists, just tokens and node children.
    */
   static class CalculatedSyntaxModel {


       final List<CsmElement> elements;


       CalculatedSyntaxModel(List<CsmElement> elements) {
           this.elements = elements;
       }


       public CalculatedSyntaxModel from(int index) {
           return new CalculatedSyntaxModel(new ArrayList<>(elements.subList(index, elements.size())));
       }


       @Override
       public String toString() {
           return "CalculatedSyntaxModel{" + "elements=" + elements + '}';
       }


       CalculatedSyntaxModel sub(int start, int end) {
           return new CalculatedSyntaxModel(elements.subList(start, end));
       }


       void removeIndentationElements() {
           elements.removeIf(el -> el instanceof CsmIndent || el instanceof CsmUnindent);
       }
   }


   public static class CsmChild implements CsmElement {


       private final Node child;


       public Node getChild() {
           return child;
       }


       CsmChild(Node child) {
           this.child = child;
       }


       @Override
       public void prettyPrint(Node node, SourcePrinter printer) {
           throw new UnsupportedOperationException("The prettyPrint method is not supported or implemented");
       }


       /*
        * Verifies if the content of the {@code CsmElement} is the same as the provided {@code TextElement}
        */
       @Override
       public boolean isCorrespondingElement(TextElement textElement) {
           return (textElement instanceof ChildTextElement)
                   && ((ChildTextElement)textElement).getChild() == getChild();
       }


       @Override
       public String toString() {
           return "child(" + child.getClass().getSimpleName() + ")";
       }


       @Override
       public boolean equals(Object o) {
           if (this == o)
               return true;
           if (o == null || getClass() != o.getClass())
               return false;
           CsmChild csmChild = (CsmChild) o;
           return child.equals(csmChild.child);
       }


       @Override
       public int hashCode() {
           return child.hashCode();
       }
   }


   List<DifferenceElement> calculateListRemovalDifference(ObservableProperty observableProperty, NodeList<?> nodeList, int index) {
       Node container = nodeList.getParentNodeForChildren();
       CsmElement element = ConcreteSyntaxModel.forClass(container.getClass());
       CalculatedSyntaxModel original = calculatedSyntaxModelForNode(element, container);
       CalculatedSyntaxModel after = calculatedSyntaxModelAfterListRemoval(element, observableProperty, nodeList, index);
       return DifferenceElementCalculator.calculate(original, after);
   }


   List<DifferenceElement> calculateListAdditionDifference(ObservableProperty observableProperty, NodeList<?> nodeList, int index, Node nodeAdded) {
       Node container = nodeList.getParentNodeForChildren();
       CsmElement element = ConcreteSyntaxModel.forClass(container.getClass());
       CalculatedSyntaxModel original = calculatedSyntaxModelForNode(element, container);
       CalculatedSyntaxModel after = calculatedSyntaxModelAfterListAddition(element, observableProperty, nodeList, index, nodeAdded);
       List<DifferenceElement> differenceElements = DifferenceElementCalculator.calculate(original, after);
       // Set the line separator character tokens
       LineSeparator lineSeparator = container.getLineEndingStyleOrDefault(LineSeparator.SYSTEM);
       replaceEolTokens(differenceElements, lineSeparator);
       return differenceElements;
   }


   /*
    * Replace EOL token in the list of {@code DifferenceElement} by the specified line separator
    */
   private void replaceEolTokens(List<DifferenceElement> differenceElements, LineSeparator lineSeparator) {
       CsmElement eol = getNewLineToken(lineSeparator);
       for (int i = 0; i < differenceElements.size(); i++) {
           DifferenceElement differenceElement = differenceElements.get(i);
           differenceElements.set(i, differenceElement.replaceEolTokens(eol));
       }
   }


   /*
    * Returns a new line token
    */
   private CsmElement getNewLineToken(LineSeparator lineSeparator) {
       return CsmElement.newline(lineSeparator);
   }


   List<DifferenceElement> calculateListReplacementDifference(ObservableProperty observableProperty, NodeList<?> nodeList, int index, Node newValue) {
       Node container = nodeList.getParentNodeForChildren();
       CsmElement element = ConcreteSyntaxModel.forClass(container.getClass());
       CalculatedSyntaxModel original = calculatedSyntaxModelForNode(element, container);
       CalculatedSyntaxModel after = calculatedSyntaxModelAfterListReplacement(element, observableProperty, nodeList, index, newValue);
       return DifferenceElementCalculator.calculate(original, after);
   }


   void calculatePropertyChange(NodeText nodeText, Node observedNode, ObservableProperty property, Object oldValue, Object newValue) {
       if (nodeText == null) {
           throw new NullPointerException();
       }
       CsmElement element = ConcreteSyntaxModel.forClass(observedNode.getClass());
       CalculatedSyntaxModel original = calculatedSyntaxModelForNode(element, observedNode);
       CalculatedSyntaxModel after = calculatedSyntaxModelAfterPropertyChange(element, observedNode, property, oldValue, newValue);
       List<DifferenceElement> differenceElements = DifferenceElementCalculator.calculate(original, after);
       Difference difference = new Difference(differenceElements, nodeText, observedNode);
       difference.apply();
   }


   CalculatedSyntaxModel calculatedSyntaxModelForNode(CsmElement csm, Node node) {
       List<CsmElement> elements = new LinkedList<>();
       calculatedSyntaxModelForNode(csm, node, elements, new NoChange());
       return new CalculatedSyntaxModel(elements);
   }


   CalculatedSyntaxModel calculatedSyntaxModelForNode(Node node) {
       return calculatedSyntaxModelForNode(ConcreteSyntaxModel.forClass(node.getClass()), node);
   }


   private void writeToFile(int[] flags) {
       // Create a StringBuilder to store the flags
       StringBuilder sb = new StringBuilder();
       for (int i = 0; i < flags.length; i++) {
           sb.append(flags[i]);
       }
       // Create a buffer writer to write the flags to a file
       BufferedWriter writer = null;
       try {
           writer = new BufferedWriter(new FileWriter("flags.txt"));
           writer.write(sb.toString());
           writer.close();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }


   private int[] loadFromFile() {
       // Create a buffer reader to read the flags from a file
       Scanner scanner = null;
       // If file exists, read the flags from the file
       
       try {
           scanner = new Scanner(new File("flags.txt")); // Correct way to read from a file
           // If empty, create a new file with the default flags
           if (!scanner.hasNext()) {
               scanner.close();
               return new int[39];
           }
       } catch (IOException e) { //
           return new int[39]; // If file not found, return default flags
       }
 
       // Create an array to store the flags
       int[] flags = new int[39];
       int i = 0;
       while (scanner.hasNextInt()) {
           flags[i++] = scanner.nextInt();
       }
       scanner.close();
       return flags;
   }


   public void calculatedSyntaxModelForNode(CsmElement csm, Node node, List<CsmElement> elements, Change change) {
       //int[] flags = loadFromFile();
       writeToFile(flags);
       if (csm instanceof CsmSequence) {
           flags[1] = 1;
           CsmSequence csmSequence = (CsmSequence) csm;
           csmSequence.getElements().forEach(e -> calculatedSyntaxModelForNode(e, node, elements, change));
       } else if (csm instanceof CsmComment) {
           flags[2] = 1;
           // nothing to do
       } else if (csm instanceof CsmSingleReference) {
           flags[3] = 1;
           CsmSingleReference csmSingleReference = (CsmSingleReference) csm;
           Node child;
           if (change instanceof PropertyChange && ((PropertyChange) change).getProperty() == csmSingleReference.getProperty()) {
               flags[4] = 1;
               flags[5] = 1;
               child = (Node) ((PropertyChange) change).getNewValue();
               if (node instanceof LambdaExpr && child instanceof ExpressionStmt) {
                   flags[6] = 1;
                   flags[7] = 1;
                   // Same edge-case as in DefaultPrettyPrinterVisitor.visit(LambdaExpr, Void)
                   child = ((ExpressionStmt) child).getExpression();
               }
           } else {
                flags[8] = 1; 
               child = csmSingleReference.getProperty().getValueAsSingleReference(node);
           }
           if (child != null) {
               flags[9] = 1;
               elements.add(new CsmChild(child));
           }
       } else if (csm instanceof CsmNone) {
           flags[10] = 1;
           // nothing to do
       } else if (csm instanceof CsmToken) {
           flags[11] = 1;
           elements.add(csm);
       } else if (csm instanceof CsmOrphanCommentsEnding) {
           flags[12] = 1;
           // nothing to do
       } else if (csm instanceof CsmList) {
           flags[13] = 1;
           CsmList csmList = (CsmList) csm;
           if (csmList.getProperty().isAboutNodes()) {
               flags[14] = 1;
               Object rawValue = change.getValue(csmList.getProperty(), node);
               NodeList<?> nodeList;
               if (rawValue instanceof Optional) {
                   flags[15] = 1;
                   Optional<?> optional = (Optional<?>) rawValue;
                   if (optional.isPresent()) {
                       flags[16] = 1;
                       if (!(optional.get() instanceof NodeList)) {
                           flags[17] = 1;
                           writeToFile(flags);
                           throw new IllegalStateException("Expected NodeList, found " + optional.get().getClass().getCanonicalName());
                       }
                       nodeList = (NodeList<?>) optional.get();
                   } else {
                        flags[18] = 1;
                       nodeList = new NodeList<>();
                   }
               } else {
                   if (!(rawValue instanceof NodeList)) {
                       flags[19] = 1;
                       writeToFile(flags);
                       throw new IllegalStateException("Expected NodeList, found " + rawValue.getClass().getCanonicalName());
                   }
                   nodeList = (NodeList<?>) rawValue;
               }
               if (!nodeList.isEmpty()) {
                   flags[20] = 1;
                   calculatedSyntaxModelForNode(csmList.getPreceeding(), node, elements, change);
                   for (int i = 0; i < nodeList.size(); i++) {
                       if (i != 0) {
                           flags[21] = 1;
                           calculatedSyntaxModelForNode(csmList.getSeparatorPre(), node, elements, change);
                       }
                       elements.add(new CsmChild(nodeList.get(i)));
                       if (i != (nodeList.size() - 1)) {
                           flags[22] = 1;
                           calculatedSyntaxModelForNode(csmList.getSeparatorPost(), node, elements, change);
                       }
                   }
                   calculatedSyntaxModelForNode(csmList.getFollowing(), node, elements, change);
               }
           } else {
                flags[23] = 1;
               Collection<?> collection = (Collection<?>) change.getValue(csmList.getProperty(), node);
               if (!collection.isEmpty()) {
                   flags[24] = 1;
                   calculatedSyntaxModelForNode(csmList.getPreceeding(), node, elements, change);
                   boolean first = true;
                   for (Iterator<?> it = collection.iterator(); it.hasNext(); ) {
                       if (!first) {
                           flags[25] = 1;
                           calculatedSyntaxModelForNode(csmList.getSeparatorPre(), node, elements, change);
                       }
                       Object value = it.next();
                       if (value instanceof Modifier) {
                           flags[26] = 1;
                           Modifier modifier = (Modifier) value;
                           elements.add(new CsmToken(toToken(modifier)));
                       } else {
                            flags[27] = 1;
                           writeToFile(flags);
                           throw new UnsupportedOperationException("Not supported value found: " + it.next().getClass().getSimpleName());
                       }
                       if (it.hasNext()) {
                           flags[28] = 1;
                           calculatedSyntaxModelForNode(csmList.getSeparatorPost(), node, elements, change);
                       }
                       first = false;
                   }
                   calculatedSyntaxModelForNode(csmList.getFollowing(), node, elements, change);
               }
           }
       } else if (csm instanceof CsmConditional) {
           flags[29] = 1;
           CsmConditional csmConditional = (CsmConditional) csm;
           boolean satisfied = change.evaluate(csmConditional, node);
           if (satisfied) {
               flags[30] = 1;
               calculatedSyntaxModelForNode(csmConditional.getThenElement(), node, elements, change);
           } else {
                flags[31] = 1;
               calculatedSyntaxModelForNode(csmConditional.getElseElement(), node, elements, change);
           }
       } else if (csm instanceof CsmIndent) {
           flags[32] = 1;
           elements.add(csm);
       } else if (csm instanceof CsmUnindent) {
           flags[33] = 1;
           elements.add(csm);
       } else if (csm instanceof CsmAttribute) {
           flags[34] = 1;
           CsmAttribute csmAttribute = (CsmAttribute) csm;
           Object value = change.getValue(csmAttribute.getProperty(), node);
           String text = value.toString();
           if (value instanceof Stringable) {
               flags[35] = 1;
               text = ((Stringable) value).asString();
           }
           elements.add(new CsmToken(csmAttribute.getTokenType(node, value.toString(), text), text));
       } else if ((csm instanceof CsmString) && (node instanceof StringLiteralExpr)) {
           flags[36] = 1;
           flags[37] = 1;
           // fix #2382:
           // This method calculates the syntax model _after_ the change has been applied.
           // If the given change is a PropertyChange, the returned model should
           // contain the new value, otherwise the original/current value should be used.
           if (change instanceof PropertyChange) {
               flags[38] = 1;
               elements.add(new CsmToken(GeneratedJavaParserConstants.STRING_LITERAL, "\"" + ((PropertyChange) change).getNewValue() + "\""));
           } else {
                flags[39] = 1;
               elements.add(new CsmToken(GeneratedJavaParserConstants.STRING_LITERAL, "\"" + ((StringLiteralExpr) node).getValue() + "\""));
           }
       } else if ((csm instanceof CsmString) && (node instanceof TextBlockLiteralExpr)) {
           flags[40] = 1;
           flags[41] = 1;
           // Per https://openjdk.java.net/jeps/378#1--Line-terminators, any 'CRLF' and 'CR' are turned into 'LF' before interpreting the text
           String eol = node.getLineEndingStyle().toString();
           // FIXME: csm should be CsmTextBlock -- See also #2677
           if (change instanceof PropertyChange) {
               flags[42] = 1;
               elements.add(new CsmToken(GeneratedJavaParserConstants.TEXT_BLOCK_LITERAL, "\"\"\"" + eol + ((PropertyChange) change).getNewValue() + "\"\"\""));
           } else {
                flags[43] = 1;
               elements.add(new CsmToken(GeneratedJavaParserConstants.TEXT_BLOCK_LITERAL, "\"\"\"" + eol + ((TextBlockLiteralExpr) node).getValue() + "\"\"\""));
           }
       } else if ((csm instanceof CsmChar) && (node instanceof CharLiteralExpr)) {
           flags[44] = 1;
           flags[45] = 1;
           if (change instanceof PropertyChange) {
               flags[46] = 1;
               elements.add(new CsmToken(GeneratedJavaParserConstants.CHAR, "'" + ((PropertyChange) change).getNewValue() + "'"));
           } else {
                flags[47] = 1;
               elements.add(new CsmToken(GeneratedJavaParserConstants.CHAR, "'" + ((CharLiteralExpr) node).getValue() + "'"));
           }
       } else if (csm instanceof CsmMix) {
           flags[48] = 1;
           CsmMix csmMix = (CsmMix) csm;
           List<CsmElement> mixElements = new LinkedList<>();
           csmMix.getElements().forEach(e -> calculatedSyntaxModelForNode(e, node, mixElements, change));
           elements.add(new CsmMix(mixElements));
       } else if (csm instanceof CsmChild) {
           flags[49] = 1;
           elements.add(csm);
       } else {
            flags[50] = 1;
           writeToFile(flags);
           throw new UnsupportedOperationException("Not supported element type: " + csm.getClass().getSimpleName() + " " + csm);
       }
       // flags[38] = -1;
       writeToFile(flags);
   }


   public static int toToken(Modifier modifier) {
       switch(modifier.getKeyword()) {
           case PUBLIC:
               return GeneratedJavaParserConstants.PUBLIC;
           case PRIVATE:
               return GeneratedJavaParserConstants.PRIVATE;
           case PROTECTED:
               return GeneratedJavaParserConstants.PROTECTED;
           case STATIC:
               return GeneratedJavaParserConstants.STATIC;
           case FINAL:
               return GeneratedJavaParserConstants.FINAL;
           case ABSTRACT:
               return GeneratedJavaParserConstants.ABSTRACT;
           case TRANSIENT:
               return GeneratedJavaParserConstants.TRANSIENT;
           case SYNCHRONIZED:
               return GeneratedJavaParserConstants.SYNCHRONIZED;
           case VOLATILE:
               return GeneratedJavaParserConstants.VOLATILE;
           case NATIVE:
               return GeneratedJavaParserConstants.NATIVE;
           case STRICTFP:
               return GeneratedJavaParserConstants.STRICTFP;
           case TRANSITIVE:
               return GeneratedJavaParserConstants.TRANSITIVE;
           default:
               throw new UnsupportedOperationException("Not supported keyword" + modifier.getKeyword().name());
       }
   }


   // /
   // / Methods that calculate CalculatedSyntaxModel
   // /
   // Visible for testing
   CalculatedSyntaxModel calculatedSyntaxModelAfterPropertyChange(Node node, ObservableProperty property, Object oldValue, Object newValue) {
       return calculatedSyntaxModelAfterPropertyChange(ConcreteSyntaxModel.forClass(node.getClass()), node, property, oldValue, newValue);
   }


   // Visible for testing
   CalculatedSyntaxModel calculatedSyntaxModelAfterPropertyChange(CsmElement csm, Node node, ObservableProperty property, Object oldValue, Object newValue) {
       List<CsmElement> elements = new LinkedList<>();
       calculatedSyntaxModelForNode(csm, node, elements, new PropertyChange(property, oldValue, newValue));
       return new CalculatedSyntaxModel(elements);
   }


   // Visible for testing
   CalculatedSyntaxModel calculatedSyntaxModelAfterListRemoval(CsmElement csm, ObservableProperty observableProperty, NodeList<?> nodeList, int index) {
       List<CsmElement> elements = new LinkedList<>();
       Node container = nodeList.getParentNodeForChildren();
       calculatedSyntaxModelForNode(csm, container, elements, new ListRemovalChange(observableProperty, index));
       return new CalculatedSyntaxModel(elements);
   }


   // Visible for testing
   CalculatedSyntaxModel calculatedSyntaxModelAfterListAddition(CsmElement csm, ObservableProperty observableProperty, NodeList<?> nodeList, int index, Node nodeAdded) {
       List<CsmElement> elements = new LinkedList<>();
       Node container = nodeList.getParentNodeForChildren();
       calculatedSyntaxModelForNode(csm, container, elements, new ListAdditionChange(observableProperty, index, nodeAdded));
       return new CalculatedSyntaxModel(elements);
   }


   // Visible for testing
   CalculatedSyntaxModel calculatedSyntaxModelAfterListAddition(Node container, ObservableProperty observableProperty, int index, Node nodeAdded) {
       CsmElement csm = ConcreteSyntaxModel.forClass(container.getClass());
       Object rawValue = observableProperty.getRawValue(container);
       if (!(rawValue instanceof NodeList)) {
           throw new IllegalStateException("Expected NodeList, found " + rawValue.getClass().getCanonicalName());
       }
       NodeList<?> nodeList = (NodeList<?>) rawValue;
       return calculatedSyntaxModelAfterListAddition(csm, observableProperty, nodeList, index, nodeAdded);
   }


   // Visible for testing
   CalculatedSyntaxModel calculatedSyntaxModelAfterListRemoval(Node container, ObservableProperty observableProperty, int index) {
       CsmElement csm = ConcreteSyntaxModel.forClass(container.getClass());
       Object rawValue = observableProperty.getRawValue(container);
       if (!(rawValue instanceof NodeList)) {
           throw new IllegalStateException("Expected NodeList, found " + rawValue.getClass().getCanonicalName());
       }
       NodeList<?> nodeList = (NodeList<?>) rawValue;
       return calculatedSyntaxModelAfterListRemoval(csm, observableProperty, nodeList, index);
   }


   // Visible for testing
   private CalculatedSyntaxModel calculatedSyntaxModelAfterListReplacement(CsmElement csm, ObservableProperty observableProperty, NodeList<?> nodeList, int index, Node newValue) {
       List<CsmElement> elements = new LinkedList<>();
       Node container = nodeList.getParentNodeForChildren();
       calculatedSyntaxModelForNode(csm, container, elements, new ListReplacementChange(observableProperty, index, newValue));
       return new CalculatedSyntaxModel(elements);
   }
}

