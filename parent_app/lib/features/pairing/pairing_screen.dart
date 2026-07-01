import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:qr_flutter/qr_flutter.dart';
import 'pairing_repository.dart';

class PairingScreen extends ConsumerStatefulWidget {
  final String childId;
  final String childName;

  const PairingScreen({
    super.key,
    required this.childId,
    required this.childName,
  });

  @override
  ConsumerState<PairingScreen> createState() => _PairingScreenState();
}

class _PairingScreenState extends ConsumerState<PairingScreen> {
  bool _isLoading = true;
  String? _errorMessage;
  Map<String, dynamic>? _pairingData;

  @override
  void initState() {
    super.initState();
    _generatePairing();
  }

  Future<void> _generatePairing() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final data = await ref.read(pairingRepositoryProvider).generatePairing(
            childId: widget.childId,
          );
      if (mounted) {
        setState(() {
          _pairingData = data;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _errorMessage = 'فشل توليد رمز الربط، حاول مرة أخرى';
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('ربط جهاز ${widget.childName}'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : _errorMessage != null
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          _errorMessage!,
                          style: const TextStyle(color: Colors.red),
                          textAlign: TextAlign.center,
                        ),
                        const SizedBox(height: 16),
                        FilledButton(
                          onPressed: _generatePairing,
                          child: const Text('إعادة المحاولة'),
                        ),
                      ],
                    ),
                  )
                : Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      const Text(
                        'امسح رمز QR من جهاز الطفل أو أدخل الكود يدوياً',
                        textAlign: TextAlign.center,
                        style: TextStyle(fontSize: 15),
                      ),
                      const SizedBox(height: 24),
                      Center(
                        child: QrImageView(
                          data: _pairingData!['qrPayload'] as String,
                          version: QrVersions.auto,
                          size: 220,
                        ),
                      ),
                      const SizedBox(height: 24),
                      Container(
                        padding: const EdgeInsets.symmetric(
                          vertical: 16,
                          horizontal: 24,
                        ),
                        decoration: BoxDecoration(
                          color: Theme.of(context)
                              .colorScheme
                              .surfaceContainerHighest,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Column(
                          children: [
                            const Text(
                              'الكود اليدوي',
                              style: TextStyle(fontSize: 13),
                            ),
                            const SizedBox(height: 8),
                            Text(
                              _pairingData!['code'] as String,
                              style: const TextStyle(
                                fontSize: 36,
                                fontWeight: FontWeight.bold,
                                letterSpacing: 8,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 16),
                      Text(
                        'ينتهي في: ${_pairingData!['expiresAt']}',
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                          fontSize: 13,
                          color: Colors.grey,
                        ),
                      ),
                      const SizedBox(height: 24),
                      OutlinedButton.icon(
                        onPressed: _generatePairing,
                        icon: const Icon(Icons.refresh),
                        label: const Text('توليد رمز جديد'),
                      ),
                    ],
                  ),
      ),
    );
  }
}
