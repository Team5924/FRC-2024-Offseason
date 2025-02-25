// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.first5924.frc2024.subsystems.elevator;

import org.first5924.frc2024.constants.ElevatorConstants;
import org.first5924.frc2024.constants.RobotConstants;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.SoftwareLimitSwitchConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.VoltageConfigs;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.StrictFollower;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

/** Add your docs here. */
public class ElevatorIOTalonFX implements ElevatorIO {
  // Leader
  private final TalonFX leftTalon = new TalonFX(ElevatorConstants.kLeftTalonId);
  // Follower
  private final TalonFX rightTalon = new TalonFX(ElevatorConstants.kRightTalonId);

  private final VoltageOut voltageOut = new VoltageOut(0).withEnableFOC(true);
  private final PositionVoltage positionVoltage = new PositionVoltage(0).withEnableFOC(true).withSlot(0);

  public ElevatorIOTalonFX() {
    MotorOutputConfigs bothMotorOutputConfigs = new MotorOutputConfigs();
    bothMotorOutputConfigs.Inverted = InvertedValue.CounterClockwise_Positive;
    bothMotorOutputConfigs.NeutralMode = NeutralModeValue.Brake;

    FeedbackConfigs bothFeedbackConfigs = new FeedbackConfigs();
    bothFeedbackConfigs.SensorToMechanismRatio = ElevatorConstants.kEncoderToSpoolRatio;

    CurrentLimitsConfigs bothCurrentLimitsConfigs = new CurrentLimitsConfigs();
    bothCurrentLimitsConfigs.SupplyCurrentLimit = 40;
    bothCurrentLimitsConfigs.SupplyCurrentLimitEnable = true;
    bothCurrentLimitsConfigs.StatorCurrentLimit = 80;

    VoltageConfigs bothVoltageConfigs = new VoltageConfigs();
    bothVoltageConfigs.PeakForwardVoltage = ElevatorConstants.kPeakForwardVoltage;
    bothVoltageConfigs.PeakReverseVoltage = ElevatorConstants.kPeakReverseVoltage ;

    SoftwareLimitSwitchConfigs defaultSoft = new SoftwareLimitSwitchConfigs();
    defaultSoft.ForwardSoftLimitEnable = true;
    defaultSoft.ForwardSoftLimitThreshold = ElevatorConstants.kForwardSoftLimitThreshold;
    defaultSoft.ReverseSoftLimitEnable = true;
    defaultSoft.ReverseSoftLimitThreshold = ElevatorConstants.kReverseSoftLimitThreshold;

    
    Slot0Configs leftSlot0Configs = new Slot0Configs();
    leftSlot0Configs.kP = ElevatorConstants.kP;
    leftSlot0Configs.GravityType = GravityTypeValue.Elevator_Static;
    leftSlot0Configs.kG = ElevatorConstants.kG;

    leftTalon.getConfigurator().apply(
      new TalonFXConfiguration()
        .withMotorOutput(bothMotorOutputConfigs)
        .withCurrentLimits(bothCurrentLimitsConfigs)
        .withFeedback(bothFeedbackConfigs)
        .withVoltage(bothVoltageConfigs)
        .withSoftwareLimitSwitch(defaultSoft)
        .withSlot0(leftSlot0Configs)
        .withClosedLoopRamps(RobotConstants.kClosedLoopRampsConfigs)
        .withOpenLoopRamps(RobotConstants.kOpenLoopRampsConfigs)
    );

    rightTalon.getConfigurator().apply(
      new TalonFXConfiguration()
        .withMotorOutput(bothMotorOutputConfigs)
        .withCurrentLimits(bothCurrentLimitsConfigs)
        .withFeedback(bothFeedbackConfigs)
        .withVoltage(bothVoltageConfigs)
        .withSoftwareLimitSwitch(defaultSoft)
        .withClosedLoopRamps(RobotConstants.kClosedLoopRampsConfigs)
        .withOpenLoopRamps(RobotConstants.kOpenLoopRampsConfigs)
    );

    leftTalon.setPosition(0);
    rightTalon.setPosition(0);
  }

  @Override
  public void updateInputs(ElevatorIOInputs inputs) {
    inputs.leftMotorTempCelsius = leftTalon.getDeviceTemp().getValueAsDouble();
    inputs.leftMotorCurrentAmps = leftTalon.getSupplyCurrent().getValueAsDouble();
    inputs.leftMotorAppliedVoltage = leftTalon.getMotorVoltage().getValueAsDouble();
    inputs.rightMotorTempCelsius = rightTalon.getDeviceTemp().getValueAsDouble();
    inputs.rightMotorCurrentAmps = rightTalon.getSupplyCurrent().getValueAsDouble();
    inputs.rightMotorAppliedVoltage = rightTalon.getMotorVoltage().getValueAsDouble();
    inputs.drumPosition = leftTalon.getPosition().getValueAsDouble();
    inputs.elevatorHeightMeters = leftTalon.getPosition().getValueAsDouble() * ElevatorConstants.kSpoolCircumferenceMeters;
  }

  @Override
  public void setElevatorHeight(double meters) {
    double spoolRotations = meters / ElevatorConstants.kSpoolCircumferenceMeters;
    leftTalon.setControl(positionVoltage.withPosition(spoolRotations));
    rightTalon.setControl(new StrictFollower(leftTalon.getDeviceID()));
  }

  @Override
  public void setVoltage(double volts) {
    leftTalon.setControl(voltageOut.withOutput(volts));
    rightTalon.setControl(new StrictFollower(leftTalon.getDeviceID()));
  }

  @Override
  public void setSoftStopOff() {
    SoftwareLimitSwitchConfigs bothSoftwareLimitSwitchConfigsOff = new SoftwareLimitSwitchConfigs();
    bothSoftwareLimitSwitchConfigsOff.ForwardSoftLimitEnable = false;
    bothSoftwareLimitSwitchConfigsOff.ReverseSoftLimitEnable = false;
    leftTalon.getConfigurator().apply(new TalonFXConfiguration().withSoftwareLimitSwitch(bothSoftwareLimitSwitchConfigsOff));
    rightTalon.getConfigurator().apply(new TalonFXConfiguration().withSoftwareLimitSwitch(bothSoftwareLimitSwitchConfigsOff));
  }

  @Override
  public void setSoftStopOn() {
    SoftwareLimitSwitchConfigs bothSoftwareLimitSwitchConfigsOn = new SoftwareLimitSwitchConfigs();
    bothSoftwareLimitSwitchConfigsOn.ForwardSoftLimitEnable = true;
    bothSoftwareLimitSwitchConfigsOn.ForwardSoftLimitThreshold = ElevatorConstants.kForwardSoftLimitThreshold;
    bothSoftwareLimitSwitchConfigsOn.ReverseSoftLimitEnable = true;
    bothSoftwareLimitSwitchConfigsOn.ReverseSoftLimitThreshold = ElevatorConstants.kReverseSoftLimitThreshold;
    leftTalon.getConfigurator().apply(new TalonFXConfiguration().withSoftwareLimitSwitch(bothSoftwareLimitSwitchConfigsOn));
    leftTalon.getConfigurator().apply(new TalonFXConfiguration().withSoftwareLimitSwitch(bothSoftwareLimitSwitchConfigsOn));
  }

  @Override
  public void setEncoder(double spoolRotations) {
    leftTalon.setPosition(spoolRotations);
    rightTalon.setPosition(spoolRotations);
  }
}