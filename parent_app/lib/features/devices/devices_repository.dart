import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/api_client.dart';
import '../../core/api_config.dart';

class DevicesRepository {
  final ApiClient _apiClient;

  DevicesRepository(this._apiClient);

  Future<List<dynamic>> getDevices() async {
    final response = await _apiClient.dio.get(ApiConfig.devices);
    return response.data as List<dynamic>;
  }
}

final devicesRepositoryProvider = Provider<DevicesRepository>((ref) {
  return DevicesRepository(ApiClient());
});
