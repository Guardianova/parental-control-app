import {
  Injectable,
  UnauthorizedException,
  NotFoundException,
} from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { PairDeviceDto } from './dto/pair-device.dto';

@Injectable()
export class DevicesService {
  constructor(private readonly prisma: PrismaService) {}

  async pair(dto: PairDeviceDto) {
    // تحديث ذري: لا ينجح إلا إذا كان usedAt لا يزال null
    // هذا يمنع استخدام نفس الرمز مرتين حتى عند طلبين متزامنين
    const updateResult = await this.prisma.pairingToken.updateMany({
      where: {
        payload: dto.qrPayload,
        usedAt: null,
        expiresAt: { gt: new Date() },
      },
      data: { usedAt: new Date() },
    });

    if (updateResult.count === 0) {
      throw new UnauthorizedException(
        'Pairing code is invalid, expired, or already used',
      );
    }

    const pairingToken = await this.prisma.pairingToken.findUnique({
      where: { payload: dto.qrPayload },
    });

    if (!pairingToken) {
      throw new NotFoundException('Pairing token not found');
    }

    const device = await this.prisma.device.create({
      data: {
        childId: pairingToken.childId,
        platform: dto.platform,
        deviceModel: dto.deviceModel,
        publicKey: dto.publicKey,
      },
    });

    return device;
  }

  async findAll(parentId: string) {
    const parent = await this.prisma.parent.findUnique({
      where: { id: parentId },
      select: { familyId: true },
    });

    if (!parent?.familyId) {
      throw new NotFoundException('No family found for this account');
    }

    return this.prisma.device.findMany({
      where: { child: { familyId: parent.familyId } },
      include: {
        child: { select: { id: true, displayName: true } },
      },
      orderBy: { pairedAt: 'desc' },
    });
  }
}
