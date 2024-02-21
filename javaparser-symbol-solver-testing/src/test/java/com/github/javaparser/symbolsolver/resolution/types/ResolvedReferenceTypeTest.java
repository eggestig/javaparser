package com.github.javaparser.symbolsolver.resolution.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javaparser.JavaParserAdapter;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.resolution.Solver;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionClassDeclaration;
import com.github.javaparser.symbolsolver.resolution.AbstractResolutionTest;
import com.github.javaparser.symbolsolver.resolution.SymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
 
class ResolvedReferenceTypeTest {
    private ReferenceTypeImpl objectRef;
    private ReferenceTypeImpl stringRef;
    private ReferenceTypeImpl fooRef;
    private ReferenceTypeImpl barRef;
    private Object object;
    private String string;
    private Foo foo;
    private Bar bar;
    private TypeSolver typeSolver;
    
    class Foo {
        private int init;

        Foo() {
            this.init = 0;
        }
    }
    
    static class Bar {
        private int init;

        Bar() {
            this.init = 0; 
        }
    }

    // ResolvedReferenceTypeTester is defined to allow to test protected method isCorrespondingBoxingType(..)
    class ResolvedReferenceTypeTester extends ReferenceTypeImpl {

        public ResolvedReferenceTypeTester(ResolvedReferenceTypeDeclaration typeDeclaration,
                                           TypeSolver typeSolver) {
            super(typeDeclaration);
        }

        @Override
        public boolean compareConsideringTypeParameters(ResolvedReferenceType other) {
            return super.compareConsideringTypeParameters(other);
        }

    }

    @BeforeEach
    void setup() {
        typeSolver = new ReflectionTypeSolver();

        objectRef = new ReferenceTypeImpl(new ReflectionClassDeclaration(Object.class, typeSolver));
        stringRef = new ReferenceTypeImpl(new ReflectionClassDeclaration(String.class, typeSolver));
        fooRef = new ReferenceTypeImpl(new ReflectionClassDeclaration(Foo.class, typeSolver));
        barRef = new ReferenceTypeImpl(new ReflectionClassDeclaration(Bar.class, typeSolver));
        
        object = new Object();
        string = new String();
        foo = new Foo();
        bar = new Bar();

    }

    @Test
    void stringInstanceRefShouldCompareToStringRef() {
        ResolvedReferenceTypeTester stringInstanceRef = new ResolvedReferenceTypeTester(
            new ReflectionClassDeclaration(String.class, typeSolver), typeSolver);

        assertTrue(stringInstanceRef.compareConsideringTypeParameters(stringRef));
    } 

    @Test
    void stringInstanceRefShouldNotCompareToObjectRef() {
        ResolvedReferenceTypeTester stringInstanceRef = new ResolvedReferenceTypeTester(
            new ReflectionClassDeclaration(string.getClass(), typeSolver), typeSolver);

        assertFalse(stringInstanceRef.compareConsideringTypeParameters(objectRef));
    } 

    @Test
    void fooInstanceRefShouldCompareToFooRef() {
        ResolvedReferenceTypeTester fooInstanceRef = new ResolvedReferenceTypeTester(
            new ReflectionClassDeclaration(foo.getClass(), typeSolver), typeSolver);

        assertTrue(fooInstanceRef.compareConsideringTypeParameters(fooRef));
    } 

    @Test
    void barInstanceRefShouldNotCompareToFooRef() {
        ResolvedReferenceTypeTester barInstanceRef = new ResolvedReferenceTypeTester(
            new ReflectionClassDeclaration(bar.getClass(), typeSolver), typeSolver);

        assertFalse(barInstanceRef.compareConsideringTypeParameters(fooRef));
    } 
 }
 