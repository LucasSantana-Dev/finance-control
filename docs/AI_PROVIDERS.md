# AI Providers Configuration Guide

This document describes the available AI providers for financial predictions, their configuration, security considerations, and usage guidelines.

## Overview

The Finance Control application supports multiple AI providers for generating financial predictions and recommendations. Currently, two providers are supported:

1. **OpenAI** (Default) - Direct integration with OpenAI's API
2. **CometAPI** (Optional) - Unified API providing access to 500+ AI models

## Provider Selection

The application uses Spring's dependency injection to select the appropriate provider:

- **OpenAI** is marked as `@Primary`, making it the default provider when both are configured
- **CometAPI** is available as an alternative when OpenAI is disabled or when you want to use CometAPI's model aggregation

### Configuration-Based Selection

To switch providers, configure the following in `application.yml` or environment variables:

```yaml
app:
  ai:
    enabled: true
    provider: openai  # or cometapi (informational, actual selection via enabled flags)
    openai:
      enabled: true   # Set to false to disable OpenAI
    cometapi:
      enabled: false  # Set to true to enable CometAPI
```

**Provider Selection Logic:**
- If both providers are enabled, OpenAI is used (via `@Primary`)
- If only one provider is enabled, that provider is used
- If neither provider is enabled, financial predictions are unavailable

## OpenAI Configuration

### Environment Variables

```bash
# OpenAI Configuration (default)
OPENAI_ENABLED=true
OPENAI_API_KEY=your-openai-api-key
OPENAI_MODEL=gpt-4o-mini
OPENAI_MAX_TOKENS=800
OPENAI_TEMPERATURE=0.2
OPENAI_BASE_URL=https://api.openai.com/v1
```

### Configuration Properties

- **enabled**: Enable/disable OpenAI provider (default: `true`)
- **apiKey**: Your OpenAI API key (required when enabled)
- **model**: Model to use (default: `gpt-4o-mini`)
- **maxTokens**: Maximum tokens in response (default: `800`)
- **temperature**: Sampling temperature (default: `0.2`)
- **baseUrl**: OpenAI API base URL (default: `https://api.openai.com/v1`)

### Features

- Direct integration with OpenAI's API
- Uses OpenAI's Responses API endpoint
- Reliable and well-documented
- Industry-standard security practices

### Cost

- Pay-per-use pricing based on tokens
- See [OpenAI Pricing](https://openai.com/pricing) for current rates

## CometAPI Configuration

### Environment Variables

```bash
# CometAPI Configuration (optional)
COMETAPI_ENABLED=false
COMETAPI_API_KEY=your-cometapi-api-key
COMETAPI_MODEL=gpt-4o-mini
COMETAPI_MAX_TOKENS=800
COMETAPI_TEMPERATURE=0.7
COMETAPI_BASE_URL=https://api.cometapi.com/v1
```

### Configuration Properties

- **enabled**: Enable/disable CometAPI provider (default: `false`)
- **apiKey**: Your CometAPI API key (required when enabled)
- **model**: Model to use - supports 500+ models (default: `gpt-4o-mini`)
- **maxTokens**: Maximum tokens in response (default: `800`)
- **temperature**: Sampling temperature (default: `0.7`)
- **baseUrl**: CometAPI base URL (default: `https://api.cometapi.com/v1`)

### Features

- **Unified Access**: Single API for 500+ AI models from OpenAI, Google, Anthropic, and more
- **OpenAI-Compatible**: Uses standard OpenAI chat completions format
- **Cost Savings**: Claims 20% cheaper than direct provider pricing
- **Model Flexibility**: Easy switching between different AI models
- **High Concurrency**: Designed for high-volume API requests

### Supported Models

CometAPI supports models from multiple providers:
- OpenAI: `gpt-4o-mini`, `gpt-4o`, `gpt-4-turbo`, etc.
- Google: `gemini-pro`, `gemini-ultra`, etc.
- Anthropic: `claude-3-opus`, `claude-3-sonnet`, etc.
- And 500+ more models

See [CometAPI Documentation](https://apidoc.cometapi.com/) for the complete list.

### Cost

- Subscription-based pricing
- Claims 20% cheaper than direct provider pricing
- Pay-as-you-go model
- See [CometAPI Pricing](https://www.cometapi.com/pricing) for current rates

## Security Considerations

### General Security Best Practices

1. **API Key Management**
   - Never commit API keys to version control
   - Use environment variables or secrets management
   - Rotate API keys regularly
   - Use different keys for development and production

2. **Data Minimization**
   - Only send necessary data to AI providers
   - Financial predictions use transaction summaries, not full records
   - Avoid sending sensitive personal information

3. **Encryption**
   - All API traffic uses TLS 1.3 encryption
   - Verify HTTPS is used for all API calls

4. **Monitoring**
   - Monitor API usage and costs
   - Set up alerts for unusual activity
   - Track response times and error rates

### OpenAI Security

**Strengths:**
- Industry-leading security practices
- Transparent data handling policies
- SOC 2 Type II certified
- GDPR compliant
- Clear data retention policies

**Considerations:**
- Data is processed by OpenAI's infrastructure
- Review OpenAI's data processing agreement
- Consider data residency requirements

### CometAPI Security

**Security Measures:**
- TLS 1.3 encryption for all traffic
- GDPR and ISO 27001 compliance claimed
- API keys stored in hardware-backed key vaults
- Regular security audits

**Security Concerns:**

1. **Hidden Domain Owner Information**
   - CometAPI's domain owner information is hidden in WHOIS database
   - Reduces transparency about who operates the service
   - Makes it harder to verify legitimacy and contact for security issues

2. **API Proxy Service Risks**
   - All API requests and responses pass through CometAPI's infrastructure
   - CometAPI has access to:
     - Your API keys (stored on their servers)
     - Request data (financial transaction summaries, user prompts)
     - Response data (AI-generated predictions)
   - Potential for data interception, logging, or misuse

3. **Related Product Security Issues**
   - Comet browser (same company) had MCP API vulnerabilities
   - Allowed unauthorized command execution
   - Raises questions about security practices across their products

4. **Single Point of Failure**
   - If CometAPI goes down or is compromised, all AI features break
   - No direct fallback to original providers without code changes

**Risk Assessment:**

- **For Non-Production Use**: **LOW-MEDIUM RISK**
  - App is not in production yet
  - Limited sensitive data exposure
  - Good opportunity to test and evaluate

- **For Production Use**: **MEDIUM-HIGH RISK**
  - Requires thorough security audit
  - Need data processing agreements
  - Should have fallback mechanism
  - Consider data residency requirements

**Mitigation Strategies:**

- **Data Minimization**: Only send necessary data (transaction summaries, not full records)
- **Encryption**: All traffic uses TLS 1.3 (verified)
- **API Key Rotation**: Regularly rotate CometAPI keys
- **Monitoring**: Track API usage and response times
- **Fallback Configuration**: Keep OpenAI as default, enable CometAPI only when needed
- **Compliance**: Verify GDPR/ISO 27001 compliance matches your requirements
- **Security Audit**: Conduct thorough security review before production use
- **Data Processing Agreement**: Review and sign data processing agreement if available

## Usage Guidelines

### When to Use OpenAI

- **Production environments** requiring maximum reliability
- **Compliance-sensitive** applications
- **Standard use cases** where model flexibility isn't critical
- **When transparency** about data handling is important

### When to Use CometAPI

- **Cost optimization** is a priority (20% savings claimed)
- **Model experimentation** is needed (access to 500+ models)
- **Non-production** environments for testing
- **When OpenAI** has availability issues (fallback option)
- **When model flexibility** is more important than direct provider relationship

### Best Practices

1. **Start with OpenAI**: Use OpenAI as the default for production
2. **Test CometAPI**: Evaluate CometAPI in non-production environments first
3. **Monitor Costs**: Track usage and costs for both providers
4. **Compare Quality**: Evaluate response quality from both providers
5. **Have Fallback**: Keep OpenAI enabled as fallback when using CometAPI
6. **Security Review**: Conduct security review before enabling CometAPI in production

## Troubleshooting

### Common Issues

#### "AI predictions are not enabled"

**Cause**: No AI provider is enabled or configured.

**Solution**:
- Ensure at least one provider is enabled in configuration
- Verify API keys are set correctly
- Check that `app.ai.enabled=true`

#### "API key must be configured"

**Cause**: API key is missing or empty.

**Solution**:
- Set the appropriate API key environment variable
- Verify the key is correct and has proper permissions
- Check for typos in environment variable names

#### "Rate limit exceeded" (CometAPI)

**Cause**: Too many requests to CometAPI.

**Solution**:
- Wait for rate limit window to reset
- Consider upgrading CometAPI subscription
- Implement request throttling in your application

#### "Authentication failed" (CometAPI)

**Cause**: Invalid or expired API key.

**Solution**:
- Verify API key is correct
- Check if key has expired
- Generate a new API key if needed

#### "Service is temporarily unavailable" (CometAPI)

**Cause**: CometAPI service is down or experiencing issues.

**Solution**:
- Check CometAPI status page
- Wait and retry the request
- Consider falling back to OpenAI if configured

### Debugging

Enable debug logging to troubleshoot issues:

```yaml
logging:
  level:
    com.finance_control.dashboard.service: DEBUG
```

This will log:
- API requests and responses
- Configuration values (without sensitive data)
- Error details

## Cost Comparison

### OpenAI Direct Pricing (Approximate)

- GPT-4o-mini: ~$0.15 per 1M input tokens, ~$0.60 per 1M output tokens
- See [OpenAI Pricing](https://openai.com/pricing) for current rates

### CometAPI Pricing (Approximate)

- Claims 20% cheaper than direct provider pricing
- Subscription-based with pay-as-you-go
- See [CometAPI Pricing](https://www.cometapi.com/pricing) for current rates

**Note**: Actual costs depend on usage patterns, model selection, and subscription tier. Always verify current pricing from official sources.

## Migration Guide

### Switching from OpenAI to CometAPI

1. **Obtain CometAPI Key**
   - Sign up at [CometAPI](https://www.cometapi.com/)
   - Generate an API key from your account

2. **Update Configuration**
   ```yaml
   app:
     ai:
       openai:
         enabled: false
       cometapi:
         enabled: true
         api-key: ${COMETAPI_API_KEY}
   ```

3. **Set Environment Variable**
   ```bash
   export COMETAPI_API_KEY=your-cometapi-key
   ```

4. **Restart Application**
   - Restart the application to load new configuration

5. **Test Functionality**
   - Test financial predictions endpoint
   - Verify response quality
   - Monitor for errors

### Switching Back to OpenAI

1. **Update Configuration**
   ```yaml
   app:
     ai:
       openai:
         enabled: true
       cometapi:
         enabled: false
   ```

2. **Restart Application**
   - Restart to apply changes

## Future Enhancements

Potential future improvements:

- **Automatic Fallback**: Automatically fallback to OpenAI if CometAPI fails
- **Provider Load Balancing**: Distribute requests across multiple providers
- **Cost Optimization**: Automatically select provider based on cost
- **Quality Comparison**: Compare response quality and select best provider
- **More Providers**: Add support for additional AI providers (Anthropic direct, Google, etc.)

## References

- [OpenAI API Documentation](https://platform.openai.com/docs)
- [CometAPI Documentation](https://apidoc.cometapi.com/)
- [OpenAI Pricing](https://openai.com/pricing)
- [CometAPI Pricing](https://www.cometapi.com/pricing)
- [CometAPI Security](https://www.cometapi.com/security)

## Support

For issues or questions:

- **OpenAI**: [OpenAI Support](https://help.openai.com/)
- **CometAPI**: [CometAPI Support](https://www.cometapi.com/support)
- **Application Issues**: Create an issue in the project repository
