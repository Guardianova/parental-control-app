import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { PrismaModule } from './prisma/prisma.module';
import { AuthModule } from './auth/auth.module';
import { FamiliesModule } from './families/families.module';
import { ChildrenModule } from './children/children.module';
import { PairingModule } from './pairing/pairing.module';
import { DevicesModule } from './devices/devices.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    PrismaModule,
    AuthModule,
    FamiliesModule,
    ChildrenModule,
    PairingModule,
    DevicesModule,
  ],
})
export class AppModule {}
