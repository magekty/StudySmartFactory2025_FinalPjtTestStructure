// 6) Common — TTL 캐시 & Async 커맨드
// Files: Common/TtlCache.cs, Common/AsyncRelayCommand.cs
using System;
using System.Collections.Concurrent;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Input;

namespace ErpWpf.App.Common;

public sealed class TtlCache<T>
{
    private readonly ConcurrentDictionary<string, (T Value, DateTimeOffset ExpireAt)> _map = new();
    private readonly TimeSpan _ttl;
    public TtlCache(TimeSpan ttl) => _ttl = ttl;

    public bool TryGet(string key, out T value)
    {
        if (_map.TryGetValue(key, out var e) && e.ExpireAt > DateTimeOffset.UtcNow)
        { value = e.Value; return true; }
        value = default!; return false;
    }

    public void Set(string key, T value) => _map[key] = (value, DateTimeOffset.UtcNow + _ttl);
}