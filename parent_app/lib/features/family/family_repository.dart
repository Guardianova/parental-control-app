import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/api_client.dart';
import '../../core/api_config.dart';

class FamilyRepository {
  final ApiClient _apiClient;

  FamilyRepository(this._apiClient);

  Future<Map<String, dynamic>> createFamily({String? name}) async {
    final response = await _apiClient.dio.post(
      ApiConfig.families,
      data: name != null ? {'name': name} : {},
    );
    return response.data as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> getMyFamily() async {
    final response = await _apiClient.dio.get(ApiConfig.familiesMe);
    return response.data as Map<String, dynamic>;
  }
}

final familyRepositoryProvider = Provider<FamilyRepository>((ref) {
  return FamilyRepository(ApiClient());
});
