import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/api_client.dart';
import '../../core/api_config.dart';

class ChildrenRepository {
  final ApiClient _apiClient;

  ChildrenRepository(this._apiClient);

  Future<Map<String, dynamic>> addChild({
    required String displayName,
    int? birthYear,
  }) async {
    final response = await _apiClient.dio.post(
      ApiConfig.children,
      data: {
        'displayName': displayName,
        if (birthYear != null) 'birthYear': birthYear,
      },
    );
    return response.data as Map<String, dynamic>;
  }

  Future<List<dynamic>> getChildren() async {
    final response = await _apiClient.dio.get(ApiConfig.children);
    return response.data as List<dynamic>;
  }
}

final childrenRepositoryProvider = Provider<ChildrenRepository>((ref) {
  return ChildrenRepository(ApiClient());
});
