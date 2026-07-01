import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'devices_repository.dart';

class DevicesScreen extends ConsumerStatefulWidget {
  const DevicesScreen({super.key});

  @override
  ConsumerState<DevicesScreen> createState() => _DevicesScreenState();
}

class _DevicesScreenState extends ConsumerState<DevicesScreen> {
  bool _isFetching = true;
  String? _errorMessage;
  List<dynamic> _devices = [];

  @override
  void initState() {
    super.initState();
    _loadDevices();
  }

  Future<void> _loadDevices() async {
    setState(() {
      _isFetching = true;
      _errorMessage = null;
    });

    try {
      final devices = await ref.read(devicesRepositoryProvider).getDevices();
      if (mounted) {
        setState(() {
          _devices = devices;
          _isFetching = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _errorMessage = 'فشل جلب الأجهزة، حاول مرة أخرى';
          _isFetching = false;
        });
      }
    }
  }

  String _formatDate(String isoDate) {
    try {
      final date = DateTime.parse(isoDate).toLocal();
      return '${date.year}/${date.month}/${date.day}';
    } catch (_) {
      return isoDate;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('الأجهزة المرتبطة'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadDevices,
            tooltip: 'تحديث',
          ),
        ],
      ),
      body: _isFetching
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
                        onPressed: _loadDevices,
                        child: const Text('إعادة المحاولة'),
                      ),
                    ],
                  ),
                )
              : _devices.isEmpty
                  ? const Center(
                      child: Text('لا توجد أجهزة مرتبطة بعد'),
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.all(16),
                      itemCount: _devices.length,
                      itemBuilder: (context, index) {
                        final device = _devices[index];
                        final child =
                            device['child'] as Map<String, dynamic>?;
                        final platform =
                            device['platform'] as String? ?? '';
                        final pairedAt =
                            device['pairedAt'] as String? ?? '';
                        final isActive =
                            device['isActive'] as bool? ?? false;

                        return Card(
                          margin: const EdgeInsets.only(bottom: 12),
                          child: ListTile(
                            leading: CircleAvatar(
                              child: Icon(
                                platform == 'ios'
                                    ? Icons.phone_iphone
                                    : Icons.phone_android,
                              ),
                            ),
                            title: Text(
                              child?['displayName'] ?? 'جهاز غير معروف',
                            ),
                            subtitle: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text('المنصة: $platform'),
                                Text('تاريخ الربط: ${_formatDate(pairedAt)}'),
                              ],
                            ),
                            trailing: Container(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 8,
                                vertical: 4,
                              ),
                              decoration: BoxDecoration(
                                color: isActive
                                    ? Colors.green.shade100
                                    : Colors.grey.shade200,
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: Text(
                                isActive ? 'مرتبط' : 'غير نشط',
                                style: TextStyle(
                                  color: isActive
                                      ? Colors.green.shade800
                                      : Colors.grey.shade600,
                                  fontSize: 12,
                                ),
                              ),
                            ),
                            isThreeLine: true,
                          ),
                        );
                      },
                    ),
    );
  }
}
