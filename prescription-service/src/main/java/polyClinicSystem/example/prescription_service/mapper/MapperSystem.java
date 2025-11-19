package polyClinicSystem.example.prescription_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import polyClinicSystem.example.prescription_service.dto.request.CreatePrescription;
import polyClinicSystem.example.prescription_service.dto.response.MedicalRecordResponse;
import polyClinicSystem.example.prescription_service.dto.response.PrescriptionResponse;
import polyClinicSystem.example.prescription_service.dto.response.PrescriptionSlimDTO;
import polyClinicSystem.example.prescription_service.model.MedicalRecord;
import polyClinicSystem.example.prescription_service.model.Prescription;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MapperSystem {
    @Mapping(source = "doctorKeycloakId" ,target = "doctorId")
    @Mapping(source = "patientKeycloakId",target = "patientId")
    @Mapping(source = "medicalRecord.id", target = "recordId")
    PrescriptionResponse toPrescriptionResponse(Prescription prescription);

    // Convert CreatePrescription -> Prescription (id + medicalRecord ignored)
    //cause there is relationships and mapping between medicalRecord and prescription
    //and this will get conflict to map struct
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "doctorKeycloakId", ignore = true)
    @Mapping(source = "patientId",target = "patientKeycloakId")
    Prescription toPrescription(CreatePrescription request);

    @Mapping(target = "patientId", source = "patientKeycloakId")
    @Mapping(source = "prescriptions", target = "prescriptions")
    MedicalRecordResponse toMedicalRecordResponse(MedicalRecord medicalRecord);

    PrescriptionSlimDTO toPrescriptionSlim(Prescription prescription);
}
