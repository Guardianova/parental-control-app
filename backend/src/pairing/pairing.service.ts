import { Injectable, NotFoundException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as crypto from 'crypto';
import { PrismaService } from '../prisma/prisma.service';
import { GeneratePairingDto } from './dto/generate-pairing.dto';

@Injectable()
export class PairingService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly config: ConfigService,
  ) {}

  async generate(parentId: string, dto: GeneratePairingDto) {
    const parent = await this.prisma.parent.findUnique({
      where: { id: parentId },
      select: { familyId: true },
    });

    if (!parent?.familyId) {
      throw new NotFoundException('No family found for this account');
    }

    const child = await this.prisma.child.findFirst({
      where: { id: dto.childId, familyId: parent.familyId },
    });

    if (!child) {
      throw new NotFoundException('Child not found in your family');
    }

    const code = this.generateNumericCode(6);
    const payload = `pairing:${code}:${crypto.randomBytes(24).toString('hex')}`;

    const ttlSeconds = Number(
      this.config.get<string>('PAIRING_TOKEN_TTL_SECONDS') ?? 300,
    );
    const expiresAt = new Date(Date.now() + ttlSeconds * 1000);

    const pairingToken = await this.prisma.pairingToken.create({
      data: {
        childId: child.id,
        code,
        payload,
        expiresAt,
      },
    });

    return {
      code: pairingToken.code,
      qrPayload: pairingToken.payload,
      expiresAt: pairingToken.expiresAt,
    };
  }

  private generateNumericCode(length: number): string {
    const digits = '0123456789';
    let result = '';
    for (let i = 0; i < length; i++) {
      result += digits[crypto.randomInt(0, digits.length)];
    }
    return result;
  }
}
