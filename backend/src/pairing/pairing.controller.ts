import { Controller, Post, Body, UseGuards, Req } from '@nestjs/common';
import { Request } from 'express';
import { PairingService } from './pairing.service';
import { GeneratePairingDto } from './dto/generate-pairing.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';

interface AuthenticatedRequest extends Request {
  user: { id: string };
}

@UseGuards(JwtAuthGuard)
@Controller('pairing')
export class PairingController {
  constructor(private readonly pairingService: PairingService) {}

  @Post('generate')
  generate(
    @Req() req: AuthenticatedRequest,
    @Body() dto: GeneratePairingDto,
  ) {
    return this.pairingService.generate(req.user.id, dto);
  }
}
