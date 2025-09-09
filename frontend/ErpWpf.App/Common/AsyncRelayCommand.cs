using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace ErpWpf.App.Common
{
    public sealed class AsyncRelayCommand : ICommand
    {
        private readonly Func<CancellationToken, Task> _run;
        private CancellationTokenSource? _cts;
        private bool _busy;

        public AsyncRelayCommand(Func<CancellationToken, Task> run) => _run = run;
        public bool CanExecute(object? parameter) => !_busy;
        public event EventHandler? CanExecuteChanged;

        public async void Execute(object? parameter)
        {
            if (_busy) return;
            _busy = true; CanExecuteChanged?.Invoke(this, EventArgs.Empty);
            _cts = new CancellationTokenSource();
            try { await _run(_cts.Token); }
            finally { _busy = false; CanExecuteChanged?.Invoke(this, EventArgs.Empty); }
        }

        public void Cancel() => _cts?.Cancel();
    }

}
