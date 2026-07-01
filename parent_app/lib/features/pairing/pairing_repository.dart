import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/api_client.dart';
import '../../core/api_config.dart';

class PairingRepository {
  final ApiClient _apiClient;

  PairingRepository(this._apiClient);

  Future<Map<String, dynamic>> generatePairing({
    required String childId,
  }) async {
    final response = await _apiClient.dio.post(
      ApiConfig.pairingGenerate,
      data: {'childId': childId},
    );
    return response.data as Map<String, dynamic>;
  }
}

final pairingRepositoryProvider = Provider<PairingRepository>((ref) {
  return PairingRepository(ApiClient());
});
