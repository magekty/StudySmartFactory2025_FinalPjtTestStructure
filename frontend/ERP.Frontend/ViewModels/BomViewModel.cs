using System.Collections.ObjectModel;
using System.Windows.Input;
using ERP.Frontend.Models;
using ERP.Frontend.Services;
using System.Threading.Tasks;

namespace ERP.Frontend.ViewModels
{
    public class BomViewModel : BaseViewModel
    {
        private ObservableCollection<BomLine> _bomTree;
        public ObservableCollection<BomLine> BomTree
        {
            get => _bomTree;
            set => SetProperty(ref _bomTree, value);
        }

        private readonly BomService _bomService = new BomService();

        public ICommand LoadBomCommand { get; }

        public BomViewModel()
        {
            LoadBomCommand = new RelayCommand(async o => await LoadBomAsync());
            LoadBomAsync(); // 뷰모델 생성 시 데이터 로드
        }

        private async Task LoadBomAsync()
        {
            // TODO: 실제 BOM 헤더 ID를 가져와야 합니다. 여기서는 임의의 ID를 사용합니다.
            var bomId = 1;
            var bomLines = await _bomService.GetBomLinesByBomHeaderIdAsync(bomId);

            // TODO: 계층 구조로 변환 로직 추가 필요
            BomTree = new ObservableCollection<BomLine>(bomLines);
        }
    }
}