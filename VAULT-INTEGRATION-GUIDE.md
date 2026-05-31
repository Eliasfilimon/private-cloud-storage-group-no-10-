# 🔐 Vault Integration Guide

This guide explains how to integrate Secure Cloud Storage with various secret management systems.

## Overview

The application supports three secret management providers:

1. **Environment Variables** (Default) - Development
2. **AWS Secrets Manager** - Production AWS
3. **Azure Key Vault** - Production Azure
4. **HashiCorp Vault** - Enterprise

---

## 1. Environment Variables (Default)

### Configuration
```properties
secrets.provider=environment
```

### Setup
Create a `.env` file in the project root:
```env
MASTER_ENCRYPTION_KEY=your-base64-encoded-32-byte-key
JWT_SIGNING_SECRET=your-jwt-secret-key
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

### Advantages
- ✅ Simple setup
- ✅ No external dependencies
- ✅ Good for development

### Disadvantages
- ❌ Secrets in files (security risk)
- ❌ Not suitable for production
- ❌ Difficult to rotate secrets

---

## 2. AWS Secrets Manager

### Prerequisites
- AWS Account with Secrets Manager access
- AWS CLI configured
- IAM user with `secretsmanager:GetSecretValue` permission

### Installation

1. **Add AWS SDK dependency** (already in pom.xml):
```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>secretsmanager</artifactId>
    <version>2.20.100</version>
</dependency>
```

2. **Configure application.properties**:
```properties
secrets.provider=aws
aws.region=us-east-1
```

3. **Set AWS credentials**:
```bash
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=us-east-1
```

### Create Secrets in AWS

```bash
# Create encryption key secret
aws secretsmanager create-secret \
  --name secure-cloud/master-encryption-key \
  --secret-string "your-base64-encoded-32-byte-key" \
  --region us-east-1

# Create JWT secret
aws secretsmanager create-secret \
  --name secure-cloud/jwt-signing-secret \
  --secret-string "your-jwt-secret-key" \
  --region us-east-1

# Create MinIO credentials
aws secretsmanager create-secret \
  --name secure-cloud/minio-access-key \
  --secret-string "minioadmin" \
  --region us-east-1

aws secretsmanager create-secret \
  --name secure-cloud/minio-secret-key \
  --secret-string "minioadmin123" \
  --region us-east-1
```

### Update Application Code

In your services, use the injected `SecretsProvider`:

```java
@Service
public class MyService {
    private final SecretsProvider secretsProvider;
    
    public MyService(SecretsProvider secretsProvider) {
        this.secretsProvider = secretsProvider;
    }
    
    public void doSomething() {
        String encryptionKey = secretsProvider.getSecret("MASTER_ENCRYPTION_KEY");
        // Use the secret
    }
}
```

### Advantages
- ✅ Secure secret storage
- ✅ Automatic rotation support
- ✅ Audit logging
- ✅ IAM integration
- ✅ Encryption at rest

### Disadvantages
- ❌ AWS account required
- ❌ Additional costs
- ❌ Network latency

---

## 3. Azure Key Vault

### Prerequisites
- Azure Account with Key Vault
- Azure CLI installed
- Service Principal with Key Vault access

### Installation

1. **Add Azure SDK dependencies** (already in pom.xml):
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
    <version>4.7.0</version>
</dependency>
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.10.0</version>
</dependency>
```

2. **Configure application.properties**:
```properties
secrets.provider=azure
azure.vault.url=https://mykeyvault.vault.azure.net/
```

3. **Set Azure credentials**:
```bash
export AZURE_TENANT_ID=your-tenant-id
export AZURE_CLIENT_ID=your-client-id
export AZURE_CLIENT_SECRET=your-client-secret
export AZURE_VAULT_URL=https://mykeyvault.vault.azure.net/
```

### Create Secrets in Azure

```bash
# Login to Azure
az login

# Create Key Vault (if not exists)
az keyvault create \
  --name mykeyvault \
  --resource-group myresourcegroup

# Create secrets
az keyvault secret set \
  --vault-name mykeyvault \
  --name master-encryption-key \
  --value "your-base64-encoded-32-byte-key"

az keyvault secret set \
  --vault-name mykeyvault \
  --name jwt-signing-secret \
  --value "your-jwt-secret-key"

az keyvault secret set \
  --vault-name mykeyvault \
  --name minio-access-key \
  --value "minioadmin"

az keyvault secret set \
  --vault-name mykeyvault \
  --name minio-secret-key \
  --value "minioadmin123"
```

### Advantages
- ✅ Azure native integration
- ✅ Managed identity support
- ✅ Compliance certifications
- ✅ Automatic backup
- ✅ Soft delete recovery

### Disadvantages
- ❌ Azure account required
- ❌ Additional costs
- ❌ Azure-specific

---

## 4. HashiCorp Vault (Enterprise)

### Prerequisites
- HashiCorp Vault server running
- Vault CLI installed
- Authentication method configured

### Installation

1. **Add Spring Vault dependency** (already in pom.xml):
```xml
<dependency>
    <groupId>org.springframework.vault</groupId>
    <artifactId>spring-vault-core</artifactId>
    <version>3.0.0</version>
</dependency>
```

2. **Configure application.properties**:
```properties
secrets.provider=vault
spring.cloud.vault.uri=http://localhost:8200
spring.cloud.vault.token=your-vault-token
spring.cloud.vault.kv-version=2
```

3. **Set Vault credentials**:
```bash
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=your-vault-token
```

### Create Secrets in Vault

```bash
# Authenticate
vault login

# Create secrets
vault kv put secret/secure-cloud/encryption \
  master-encryption-key="your-base64-encoded-32-byte-key"

vault kv put secret/secure-cloud/jwt \
  signing-secret="your-jwt-secret-key"

vault kv put secret/secure-cloud/minio \
  access-key="minioadmin" \
  secret-key="minioadmin123"
```

### Advantages
- ✅ Enterprise-grade security
- ✅ Dynamic secrets support
- ✅ Encryption as a service
- ✅ Audit logging
- ✅ Multi-cloud support

### Disadvantages
- ❌ Complex setup
- ❌ Requires Vault infrastructure
- ❌ Operational overhead

---

## Switching Providers

### Step 1: Update Configuration
Edit `application.properties`:
```properties
# Change from environment to aws
secrets.provider=aws
```

### Step 2: Set Provider Credentials
```bash
# For AWS
export AWS_ACCESS_KEY_ID=...
export AWS_SECRET_ACCESS_KEY=...

# For Azure
export AZURE_TENANT_ID=...
export AZURE_CLIENT_ID=...
export AZURE_CLIENT_SECRET=...
```

### Step 3: Create Secrets in New Provider
Use the provider's CLI to create secrets

### Step 4: Restart Application
```bash
docker-compose restart backend
```

---

## Health Checks

### Check Provider Health
```bash
curl http://localhost:8080/actuator/health
```

### Manual Health Check
```java
@Autowired
private SecretsProvider secretsProvider;

public void checkHealth() {
    boolean healthy = secretsProvider.isHealthy();
    System.out.println("Secrets provider is " + (healthy ? "healthy" : "unhealthy"));
}
```

---

## Best Practices

### 1. Secret Naming Convention
```
secure-cloud/[service]/[secret-name]
secure-cloud/encryption/master-key
secure-cloud/jwt/signing-secret
secure-cloud/minio/access-key
```

### 2. Rotation Strategy
- Rotate secrets every 90 days
- Use provider's rotation features
- Update application after rotation

### 3. Access Control
- Use IAM roles (AWS) or Managed Identity (Azure)
- Principle of least privilege
- Audit all secret access

### 4. Monitoring
- Enable audit logging
- Monitor failed access attempts
- Alert on suspicious activity

---

## Troubleshooting

### Secret Not Found
```
Error: Secret not found: MASTER_ENCRYPTION_KEY
```

**Solution:**
1. Verify secret exists in provider
2. Check secret name matches configuration
3. Verify IAM/authentication credentials

### Connection Timeout
```
Error: Connection timeout to secrets provider
```

**Solution:**
1. Check network connectivity
2. Verify provider endpoint URL
3. Check firewall rules

### Authentication Failed
```
Error: Authentication failed with secrets provider
```

**Solution:**
1. Verify credentials are correct
2. Check IAM permissions
3. Verify token/key hasn't expired

---

## Migration Path

### Development → Production

1. **Start with Environment Variables**
   - Easy setup
   - Good for testing

2. **Move to AWS/Azure**
   - Production-ready
   - Managed by cloud provider

3. **Enterprise: Use HashiCorp Vault**
   - Multi-cloud support
   - Advanced features

---

## Additional Resources

- [AWS Secrets Manager Documentation](https://docs.aws.amazon.com/secretsmanager/)
- [Azure Key Vault Documentation](https://docs.microsoft.com/en-us/azure/key-vault/)
- [HashiCorp Vault Documentation](https://www.vaultproject.io/docs)
- [Spring Cloud Vault](https://spring.io/projects/spring-cloud-vault)

---

**Last Updated:** May 29, 2026
