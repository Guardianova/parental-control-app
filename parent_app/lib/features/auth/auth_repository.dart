import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/api_client.dart';
import '../../core/api_config.dart';
import '../../core/secure_storage.dart';

class AuthRepository {
  final ApiClient _apiClient;
  final SecureStorage _secureStorage;

  AuthRepository(this._apiClient, this._secureStorage);

  Future<void> register({
    required String email,
    required String password,
    required String fullName,
  }) async {
    final response = await _apiClient.dio.post(
      ApiConfig.authRegister,
      data: {
        'email': email,
        'password': password,
        'fullName': fullName,
      },
    );

    final accessToken = response.data['accessToken'] as String;
    final refreshToken = response.data['refreshToken'] as String;

    await _secureStorage.saveTokens(
      accessToken: accessToken,
      refreshToken: refreshToken,
    );
  }

  Future<void> login({
    required String email,
    required String password,
  }) async {
    final response = await _apiClient.dio.post(
      ApiConfig.authLogin,
      data: {
        'email': email,
        'password': password,
      },
    );

    final accessToken = response.data['accessToken'] as String;
    final refreshToken = response.data['refreshToken'] as String;

    await _secureStorage.saveTokens(
      accessToken: accessToken,
      refreshToken: refreshToken,
    );
  }
}

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepository(ApiClient(), SecureStorage());
});
