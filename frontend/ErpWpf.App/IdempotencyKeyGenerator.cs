// ErpWpf.App/IdempotencyKeyGenerator.cs
using System;

public sealed class IdempotencyKeyGenerator
{
    public string NewKey() => Guid.NewGuid().ToString();
}

