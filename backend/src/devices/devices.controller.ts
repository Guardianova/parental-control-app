import {
  Controller,
  Post,
  Get,
  Body,
  UseGuards,
  Req,
} from '@nestjs/common';
import { Request } from 'express';
import { DevicesService } from './devices.service';
import { PairDeviceDto } from './dto/pair-device.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';

interface AuthenticatedRequest extends Request {
  user: { id: string };
}

@Controller('devices')
export class DevicesController {
  constructor(private readonly devicesService: DevicesService) {}

  @Post('pair')
  pair(@Body() dto: PairDeviceDto) {
    return this.devicesService.pair(dto);
  }

  @Get()
  @UseGuards(JwtAuthGuard)
  findAll(@Req() req: AuthenticatedRequest) {
    return this.devicesService.findAll(req.user.id);
  }
}
