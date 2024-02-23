package com.github.javaparser.resolution;

import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeTransformer;
import com.github.javaparser.resolution.types.parametrization.ResolvedTypeParametersMap;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.logic.MethodResolutionLogic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

class myclass implements ResolvedType{
    public myclass(){
        int a = 1;
    }
    @Override
    public boolean isReferenceType(){
        return true;
    }
    @Override
    public String describe() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'describe'");
    }

    @Override
    public boolean isAssignableBy(ResolvedType other) {
        return true;
    }

    @Override
    public ResolvedReferenceType asReferenceType() {
        return new class2(new class3());
    }

}

class myclass2 implements ResolvedType{
    public myclass2(){
        int a = 1;
    }
    
    @Override
    public boolean isTypeVariable(){
        return true;
    }
    @Override
    public boolean isReferenceType(){
        return true;
    }
    

  
    @Override
    public String describe() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'describe'");
    }

    @Override
    public boolean isAssignableBy(ResolvedType other) {
        return true;
    }

    @Override
    public ResolvedReferenceType asReferenceType() {
        return new class2(new class3());
    }

}

class class2 extends ResolvedReferenceType{

    public class2(ResolvedReferenceTypeDeclaration typeDeclaration) {
        super(typeDeclaration);
        //TODO Auto-generated constructor stub
    }
    

    @Override
    public ResolvedType transformTypeParameters(ResolvedTypeTransformer transformer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transformTypeParameters'");
    }

    @Override
    public boolean isAssignableBy(ResolvedType other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAssignableBy'");
    }

    @Override
    public List<ResolvedReferenceType> getAllAncestors() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllAncestors'");
    }

    @Override
    public List<ResolvedReferenceType> getAllAncestors(
            Function<ResolvedReferenceTypeDeclaration, List<ResolvedReferenceType>> traverser) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllAncestors'");
    }

    @Override
    public List<ResolvedReferenceType> getDirectAncestors() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDirectAncestors'");
    }

    @Override
    public Set<MethodUsage> getDeclaredMethods() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDeclaredMethods'");
    }

    @Override
    public Set<ResolvedFieldDeclaration> getDeclaredFields() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDeclaredFields'");
    }

    @Override
    public ResolvedType toRawType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toRawType'");
    }

    @Override
    protected ResolvedReferenceType create(ResolvedReferenceTypeDeclaration typeDeclaration,
            List<ResolvedType> typeParameters) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    protected ResolvedReferenceType create(ResolvedReferenceTypeDeclaration typeDeclaration) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public ResolvedReferenceType deriveTypeParameters(ResolvedTypeParametersMap typeParametersMap) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deriveTypeParameters'");
    }

}
class class3 implements ResolvedReferenceTypeDeclaration{

    @Override
    public Optional<ResolvedReferenceTypeDeclaration> containerType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containerType'");
    }

    @Override
    public String getPackageName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPackageName'");
    }

    @Override
    public String getClassName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getClassName'");
    }

    @Override
    public String getQualifiedName() {
        // TODO Auto-generated method stub
        return "Good!";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public List<ResolvedTypeParameterDeclaration> getTypeParameters() {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

    @Override
    public List<ResolvedReferenceType> getAncestors(boolean acceptIncompleteList) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAncestors'");
    }

    @Override
    public List<ResolvedFieldDeclaration> getAllFields() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllFields'");
    }

    @Override
    public Set<ResolvedMethodDeclaration> getDeclaredMethods() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDeclaredMethods'");
    }

    @Override
    public Set<MethodUsage> getAllMethods() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllMethods'");
    }

    @Override
    public boolean isAssignableBy(ResolvedType type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAssignableBy'");
    }

    @Override
    public boolean isAssignableBy(ResolvedReferenceTypeDeclaration other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAssignableBy'");
    }

    @Override
    public boolean hasDirectlyAnnotation(String qualifiedName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasDirectlyAnnotation'");
    }

    @Override
    public boolean isFunctionalInterface() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isFunctionalInterface'");
    }

    @Override
    public List<ResolvedConstructorDeclaration> getConstructors() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getConstructors'");
    }

}

class IsAssignableMatchTypeParametersMatchingQNameBranchCovertest {

    @Test
    void shouldAnswerWithTrue() {
        assertTrue(true);
    }    

    //test for the same param array
    @Test
    void testExpectedParamIsArray(){
        myclass expectedType = new myclass(); 
        myclass actualType = new myclass(); 
        Map<String, ResolvedType> matchedParameters = new HashMap<>();
        assertTrue(MethodResolutionLogic.isAssignableMatchTypeParameters(expectedType, actualType, matchedParameters));
        return;
    }
    // test for the Reference Variable
    @Test
    void testExpectedParamIsReferenceVariable(){
        myclass2 expectedType = new myclass2();  
        myclass2 actualType = new myclass2();   
        Map<String, ResolvedType> matchedParameters = new HashMap<>();
        assertTrue(MethodResolutionLogic.isAssignableMatchTypeParameters(expectedType, actualType, matchedParameters));
    }

   

}
