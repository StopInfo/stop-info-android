package de.nickbw2003.stopinfo.networks.ui.selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.nickbw2003.stopinfo.R
import de.nickbw2003.stopinfo.common.ui.base.dataloading.DataLoadingViewModel
import de.nickbw2003.stopinfo.networks.data.NetworkRepository
import de.nickbw2003.stopinfo.networks.data.NetworksService
import de.nickbw2003.stopinfo.networks.data.models.NetworkInfo

class NetworkSelectionViewModel(
    private val networksService: NetworksService,
    private val networkRepository: NetworkRepository
) : DataLoadingViewModel<List<NetworkInfo>>() {
    private val _currentNetwork = MutableLiveData<NetworkInfo?>()
    val currentNetwork: LiveData<NetworkInfo?>
        get() = _currentNetwork

    private val _listHeaderTitle = MutableLiveData<Int>()
    val listHeaderTitle: LiveData<Int>
        get() = _listHeaderTitle

    var isStartDestination: Boolean = false

    override fun hasData(data: List<NetworkInfo>): Boolean {
        return data.isNotEmpty()
    }

    override fun refresh() {
        super.refresh()
        loadAvailableNetworks()
    }

    fun loadAvailableNetworks() {
        launchDataLoad {
            val networks = networksService.getAvailableNetworks() ?: emptyList()
            _data.postValue(networks)
            _currentNetwork.postValue(networkRepository.currentNetwork)

            val titleResource =
                if (networks.isEmpty())
                    R.string.network_list_header_title_nothing_found
                else
                    R.string.network_list_header_title_select

            _listHeaderTitle.postValue(titleResource)
            networks
        }
    }

    fun onNetworkSelected(network: NetworkInfo) {
        networkRepository.currentNetwork = network
        performNavigation(if (isStartDestination) R.id.start_to_map else R.id.network_selection_to_map)
    }
}