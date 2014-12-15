<%@ page import="au.org.ala.ozatlasproxy.ApiKey" %>

<div class="fieldcontain ${hasErrors(bean: apiKeyInstance, field: 'dataResourceId', 'error')} ">
	<label for="dataResourceId">
		<g:message code="apiKey.dataResourceId.label" default="Data Resource Id" />
		
	</label>
	<g:textField name="dataResourceId" value="${apiKeyInstance?.dataResourceId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: apiKeyInstance, field: 'description', 'error')} ">
	<label for="description">
		<g:message code="apiKey.description.label" default="Description" />
		
	</label>
	<g:textField name="description" value="${apiKeyInstance?.description}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: apiKeyInstance, field: 'name', 'error')} ">
	<label for="name">
		<g:message code="apiKey.name.label" default="Name" />
		
	</label>
	<g:textField name="name" value="${apiKeyInstance?.name}"/>
</div>

