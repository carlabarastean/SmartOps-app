package smartHomeApp.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smartHomeApp.model.Device;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    @Query("SELECT d FROM Device d WHERE d.room.id = :roomId")
    List<Device> findDevicesByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT d FROM Device d WHERE d.status = :status")
    List<Device> findActiveDevices(@Param("status") Boolean status);
}
