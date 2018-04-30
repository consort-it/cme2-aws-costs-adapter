package de.consortit.application.security;

import org.pac4j.core.authorization.authorizer.RequireAnyAttributeAuthorizer;

public class AwsCostsAdapterAttributeAuthorizer extends RequireAnyAttributeAuthorizer {

    public AwsCostsAdapterAttributeAuthorizer(final String attribute, final String valueToMatch) {
        super(valueToMatch);
        setElements(attribute);
    }
}
